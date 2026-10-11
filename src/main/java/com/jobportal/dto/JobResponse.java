package com.jobportal.dto;

import com.jobportal.entity.Job;
import java.time.LocalDateTime;

public class JobResponse {
    private Long id;
    private String title;
    private String company;
    private String description;
    private String location;
    private String employmentType;
    private String salary;
    private String requiredSkills;
    private boolean active;
    private Long recruiterId;
    private String recruiterName;
    private LocalDateTime createdAt;
    private long applicantCount;
    private boolean hasApplied;
    private boolean saved;

    public JobResponse() {}

    public static JobResponse fromEntity(Job job, long applicantCount, boolean hasApplied) {
        return fromEntity(job, applicantCount, hasApplied, false);
    }

    public static JobResponse fromEntity(Job job, long applicantCount, boolean hasApplied, boolean saved) {
        JobResponse resp = new JobResponse();
        resp.setId(job.getId());
        resp.setTitle(job.getTitle());
        resp.setCompany(job.getCompany());
        resp.setDescription(job.getDescription());
        resp.setLocation(job.getLocation());
        resp.setEmploymentType(job.getEmploymentType());
        resp.setSalary(job.getSalary());
        resp.setRequiredSkills(job.getRequiredSkills());
        resp.setActive(job.isActive());
        if (job.getRecruiter() != null) {
            resp.setRecruiterId(job.getRecruiter().getId());
            resp.setRecruiterName(job.getRecruiter().getName());
        }
        resp.setCreatedAt(job.getCreatedAt());
        resp.setApplicantCount(applicantCount);
        resp.setHasApplied(hasApplied);
        resp.setSaved(saved);
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(Long recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getApplicantCount() {
        return applicantCount;
    }

    public void setApplicantCount(long applicantCount) {
        this.applicantCount = applicantCount;
    }

    public boolean isHasApplied() {
        return hasApplied;
    }

    public void setHasApplied(boolean hasApplied) {
        this.hasApplied = hasApplied;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
