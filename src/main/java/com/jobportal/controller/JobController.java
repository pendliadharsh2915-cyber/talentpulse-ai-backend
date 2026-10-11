package com.jobportal.controller;

import com.jobportal.dto.JobRequest;
import com.jobportal.dto.JobResponse;
import com.jobportal.dto.PageResponse;
import com.jobportal.entity.User;
import com.jobportal.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @AuthenticationPrincipal User currentUser
    ) {
        if (page != null) {
            PageResponse<JobResponse> pageResponse = jobService.getFilteredJobsPaged(
                    search, employmentType, location, Math.max(0, page), Math.max(1, size), currentUser);
            return ResponseEntity.ok(pageResponse);
        } else {
            List<JobResponse> jobs = jobService.getFilteredJobs(search, employmentType, location, currentUser);
            return ResponseEntity.ok(jobs);
        }
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        JobResponse job = jobService.getJobById(id, currentUser);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal User recruiter
    ) {
        JobResponse created = jobService.createJob(request, recruiter);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal User recruiter
    ) {
        JobResponse updated = jobService.updateJob(id, request, recruiter);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Map<String, String>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User recruiter
    ) {
        jobService.deleteJob(id, recruiter);
        return ResponseEntity.ok(Map.of("message", "Job deactivated successfully"));
    }

    @GetMapping("/recruiter/jobs")
    public ResponseEntity<List<JobResponse>> getRecruiterJobs(
            @AuthenticationPrincipal User recruiter
    ) {
        List<JobResponse> jobs = jobService.getRecruiterJobs(recruiter);
        return ResponseEntity.ok(jobs);
    }

    // --- Saved / Bookmark Endpoints ---

    @PostMapping("/jobs/{id}/save")
    public ResponseEntity<Map<String, Object>> toggleSaveJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        boolean saved = jobService.toggleSaveJob(id, user);
        return ResponseEntity.ok(Map.of(
                "saved", saved,
                "message", saved ? "Job saved to your bookmarks" : "Job removed from your bookmarks"
        ));
    }

    @GetMapping("/jobs/saved")
    public ResponseEntity<List<JobResponse>> getSavedJobs(
            @AuthenticationPrincipal User user
    ) {
        List<JobResponse> savedJobs = jobService.getSavedJobs(user);
        return ResponseEntity.ok(savedJobs);
    }
}
