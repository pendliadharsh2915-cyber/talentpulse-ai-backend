package com.jobportal.repository;

import com.jobportal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    
    List<JobApplication> findByJobSeekerIdOrderByAppliedAtDesc(Long jobSeekerId);
    
    List<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);
    
    List<JobApplication> findByJobRecruiterIdOrderByAppliedAtDesc(Long recruiterId);
    
    long countByJobSeekerId(Long jobSeekerId);
    
    long countByJobRecruiterId(Long recruiterId);
    
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    boolean existsByJobSeekerIdAndJobRecruiterId(Long jobSeekerId, Long recruiterId);
    
    Optional<JobApplication> findByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);
}
