package com.jobportal.service;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.Resume;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.ResumeRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final NotificationService notificationService;

    public ApplicationService(JobApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              ResumeRepository resumeRepository,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.notificationService = notificationService;
    }

    private Resume resolveApplicationResume(JobApplication app) {
        if (app.getResume() != null) {
            return app.getResume();
        }
        return resumeRepository.findFirstByJobSeekerIdOrderByUploadedAtDesc(app.getJobSeeker().getId()).orElse(null);
    }

    @Transactional
    public ApplicationResponse apply(Long jobId, User seeker) {
        if (seeker.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can apply for jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (!job.isActive()) {
            throw new BadRequestException("This job listing is no longer active");
        }

        if (applicationRepository.existsByJobIdAndJobSeekerId(jobId, seeker.getId())) {
            throw new BadRequestException("You have already submitted an application for this position");
        }

        // Link the exact resume submitted at application time
        Optional<Resume> resumeOpt = resumeRepository.findFirstByJobSeekerIdOrderByUploadedAtDesc(seeker.getId());

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setJobSeeker(seeker);
        application.setResume(resumeOpt.orElse(null));
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());

        JobApplication saved = applicationRepository.save(application);

        // Notify recruiter of new candidate submission
        notificationService.createNotification(
                job.getRecruiter(),
                "New Candidate Application",
                seeker.getName() + " applied for " + job.getTitle() + " (" + job.getCompany() + ")",
                "/recruiter/my-jobs"
        );

        Long resumeId = saved.getResume() != null ? saved.getResume().getId() : null;
        String resumeFilename = saved.getResume() != null ? saved.getResume().getOriginalFilename() : null;

        return ApplicationResponse.fromEntity(saved, resumeId, resumeFilename);
    }

    public List<ApplicationResponse> getMyApplications(User seeker) {
        List<JobApplication> list = applicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(seeker.getId());
        return list.stream().map(app -> {
            Resume resume = resolveApplicationResume(app);
            Long resumeId = resume != null ? resume.getId() : null;
            String resumeFilename = resume != null ? resume.getOriginalFilename() : null;
            return ApplicationResponse.fromEntity(app, resumeId, resumeFilename);
        }).collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsForJob(Long jobId, User recruiter) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You are not authorized to view applications for jobs posted by another recruiter");
        }

        List<JobApplication> list = applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId);
        return list.stream().map(app -> {
            Resume resume = resolveApplicationResume(app);
            Long resumeId = resume != null ? resume.getId() : null;
            String resumeFilename = resume != null ? resume.getOriginalFilename() : null;
            return ApplicationResponse.fromEntity(app, resumeId, resumeFilename);
        }).collect(Collectors.toList());
    }

    public List<ApplicationResponse> getAllRecruiterApplications(User recruiter) {
        List<JobApplication> list = applicationRepository.findByJobRecruiterIdOrderByAppliedAtDesc(recruiter.getId());
        return list.stream().map(app -> {
            Resume resume = resolveApplicationResume(app);
            Long resumeId = resume != null ? resume.getId() : null;
            String resumeFilename = resume != null ? resume.getOriginalFilename() : null;
            return ApplicationResponse.fromEntity(app, resumeId, resumeFilename);
        }).collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status, User recruiter) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You are not authorized to update applications for jobs belonging to another recruiter");
        }

        application.setStatus(status);
        JobApplication updated = applicationRepository.save(application);

        // Notify job seeker of status advancement
        notificationService.createNotification(
                updated.getJobSeeker(),
                "Application Status Update: " + status.name(),
                "Your application for " + updated.getJob().getTitle() + " at " + updated.getJob().getCompany() + " is now " + status.name(),
                "/seeker/applications"
        );

        Resume resume = resolveApplicationResume(updated);
        Long resumeId = resume != null ? resume.getId() : null;
        String resumeFilename = resume != null ? resume.getOriginalFilename() : null;

        return ApplicationResponse.fromEntity(updated, resumeId, resumeFilename);
    }

    public long getMyApplicationsCount(Long seekerId) {
        return applicationRepository.countByJobSeekerId(seekerId);
    }

    public long getRecruiterApplicationsCount(Long recruiterId) {
        return applicationRepository.countByJobRecruiterId(recruiterId);
    }
}
