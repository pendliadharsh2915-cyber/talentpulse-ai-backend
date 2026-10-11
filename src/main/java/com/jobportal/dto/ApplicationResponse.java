package com.jobportal.dto;

import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.JobApplication;
import java.time.LocalDateTime;

public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String employmentType;
    private String salary;
    
    // Candidate details (for recruiter view)
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateEducation;
    private String candidateSkills;
    private Long resumeId;
    private String resumeFilename;

    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    public ApplicationResponse() {}

    public static ApplicationResponse fromEntity(JobApplication app, Long resumeId, String resumeFilename) {
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(app.getId());
        
        if (app.getJob() != null) {
            resp.setJobId(app.getJob().getId());
            resp.setJobTitle(app.getJob().getTitle());
            resp.setCompany(app.getJob().getCompany());
            resp.setLocation(app.getJob().getLocation());
            resp.setEmploymentType(app.getJob().getEmploymentType());
            resp.setSalary(app.getJob().getSalary());
        }

        if (app.getJobSeeker() != null) {
            resp.setCandidateId(app.getJobSeeker().getId());
            resp.setCandidateName(app.getJobSeeker().getName());
            resp.setCandidateEmail(app.getJobSeeker().getEmail());
            resp.setCandidatePhone(app.getJobSeeker().getPhone());
            resp.setCandidateEducation(app.getJobSeeker().getEducation());
            resp.setCandidateSkills(app.getJobSeeker().getSkills());
        }

        resp.setResumeId(resumeId);
        resp.setResumeFilename(resumeFilename);
        resp.setStatus(app.getStatus());
        resp.setAppliedAt(app.getAppliedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getCandidatePhone() {
        return candidatePhone;
    }

    public void setCandidatePhone(String candidatePhone) {
        this.candidatePhone = candidatePhone;
    }

    public String getCandidateEducation() {
        return candidateEducation;
    }

    public void setCandidateEducation(String candidateEducation) {
        this.candidateEducation = candidateEducation;
    }

    public String getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(String candidateSkills) {
        this.candidateSkills = candidateSkills;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getResumeFilename() {
        return resumeFilename;
    }

    public void setResumeFilename(String resumeFilename) {
        this.resumeFilename = resumeFilename;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
