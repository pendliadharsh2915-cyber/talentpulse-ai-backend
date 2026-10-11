package com.jobportal.service;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.dto.PageResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.Role;
import com.jobportal.entity.SavedJob;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.SavedJobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SavedJobRepository savedJobRepository;

    public JobService(JobRepository jobRepository,
                      JobApplicationRepository jobApplicationRepository,
                      SavedJobRepository savedJobRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.savedJobRepository = savedJobRepository;
    }

    private JobResponse mapToResponse(Job job, User currentUser) {
        long count = jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId()).size();
        boolean hasApplied = false;
        boolean isSaved = false;

        if (currentUser != null && currentUser.getRole() == Role.JOB_SEEKER) {
            hasApplied = jobApplicationRepository.existsByJobIdAndJobSeekerId(job.getId(), currentUser.getId());
            isSaved = savedJobRepository.existsByUserIdAndJobId(currentUser.getId(), job.getId());
        }

        return JobResponse.fromEntity(job, count, hasApplied, isSaved);
    }

    public List<JobResponse> getAllJobs(String keyword, User currentUser) {
        return getFilteredJobs(keyword, null, null, currentUser);
    }

    public List<JobResponse> getFilteredJobs(String keyword, String employmentType, String location, User currentUser) {
        List<Job> jobs = jobRepository.filterJobsList(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                (employmentType != null && !employmentType.isBlank()) ? employmentType.trim() : null,
                (location != null && !location.isBlank()) ? location.trim() : null
        );

        return jobs.stream().map(job -> mapToResponse(job, currentUser)).collect(Collectors.toList());
    }

    public PageResponse<JobResponse> getFilteredJobsPaged(String keyword, String employmentType, String location,
                                                          int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> jobPage = jobRepository.filterJobs(
                (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
                (employmentType != null && !employmentType.isBlank()) ? employmentType.trim() : null,
                (location != null && !location.isBlank()) ? location.trim() : null,
                pageable
        );

        List<JobResponse> content = jobPage.getContent()
                .stream()
                .map(job -> mapToResponse(job, currentUser))
                .collect(Collectors.toList());

        return new PageResponse<>(content, jobPage.getNumber(), jobPage.getSize(),
                jobPage.getTotalElements(), jobPage.getTotalPages(), jobPage.isLast());
    }

    public JobResponse getJobById(Long id, User currentUser) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        return mapToResponse(job, currentUser);
    }

    @Transactional
    public JobResponse createJob(JobRequest req, User recruiter) {
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can post jobs");
        }

        Job job = new Job();
        job.setTitle(req.getTitle().trim());
        job.setCompany(req.getCompany().trim());
        job.setDescription(req.getDescription().trim());
        job.setLocation(req.getLocation().trim());
        job.setEmploymentType(req.getEmploymentType().trim());
        job.setSalary(req.getSalary());
        job.setRequiredSkills(req.getRequiredSkills().trim());
        job.setActive(req.getActive() != null ? req.getActive() : true);
        job.setRecruiter(recruiter);

        Job saved = jobRepository.save(job);
        return JobResponse.fromEntity(saved, 0, false, false);
    }

    @Transactional
    public JobResponse updateJob(Long id, JobRequest req, User recruiter) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        // Check ownership
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You are not authorized to edit jobs posted by other recruiters");
        }

        job.setTitle(req.getTitle().trim());
        job.setCompany(req.getCompany().trim());
        job.setDescription(req.getDescription().trim());
        job.setLocation(req.getLocation().trim());
        job.setEmploymentType(req.getEmploymentType().trim());
        job.setSalary(req.getSalary());
        job.setRequiredSkills(req.getRequiredSkills().trim());
        if (req.getActive() != null) {
            job.setActive(req.getActive());
        }

        Job updated = jobRepository.save(job);
        long count = jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId()).size();
        return JobResponse.fromEntity(updated, count, false, false);
    }

    @Transactional
    public void deleteJob(Long id, User recruiter) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));

        // Check ownership
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You are not authorized to delete or deactivate jobs posted by other recruiters");
        }

        // Soft-deactivate so existing applications are not orphaned
        job.setActive(false);
        jobRepository.save(job);
    }

    public List<JobResponse> getRecruiterJobs(User recruiter) {
        List<Job> jobs = jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiter.getId());
        return jobs.stream().map(job -> {
            long count = jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId()).size();
            return JobResponse.fromEntity(job, count, false, false);
        }).collect(Collectors.toList());
    }

    public long getTotalActiveJobsCount() {
        return jobRepository.countByActiveTrue();
    }

    public long getRecruiterJobCount(Long recruiterId) {
        return jobRepository.countByRecruiterId(recruiterId);
    }

    // --- Saved / Bookmarked Jobs Operations ---

    @Transactional
    public boolean toggleSaveJob(Long jobId, User user) {
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can save jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId)) {
            savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
            return false; // Now unsaved
        } else {
            SavedJob savedJob = new SavedJob(user, job);
            savedJobRepository.save(savedJob);
            return true; // Now saved
        }
    }

    public List<JobResponse> getSavedJobs(User user) {
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new AccessDeniedException("Only job seekers can view saved jobs");
        }

        return savedJobRepository.findByUserIdOrderBySavedAtDesc(user.getId())
                .stream()
                .map(sj -> mapToResponse(sj.getJob(), user))
                .collect(Collectors.toList());
    }
}
