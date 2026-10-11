package com.jobportal.repository;

import com.jobportal.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findFirstByJobSeekerIdOrderByUploadedAtDesc(Long jobSeekerId);
    List<Resume> findByJobSeekerIdOrderByUploadedAtDesc(Long jobSeekerId);
}
