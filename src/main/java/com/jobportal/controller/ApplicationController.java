package com.jobportal.controller;

import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.StatusUpdateRequest;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> applyToJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User seeker
    ) {
        ApplicationResponse response = applicationService.apply(jobId, seeker);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/applications/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal User seeker
    ) {
        List<ApplicationResponse> list = applicationService.getMyApplications(seeker);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/recruiter/jobs/{jobId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User recruiter
    ) {
        List<ApplicationResponse> list = applicationService.getApplicationsForJob(jobId, recruiter);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/recruiter/applications")
    public ResponseEntity<List<ApplicationResponse>> getAllRecruiterApplications(
            @AuthenticationPrincipal User recruiter
    ) {
        List<ApplicationResponse> list = applicationService.getAllRecruiterApplications(recruiter);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal User recruiter
    ) {
        ApplicationResponse updated = applicationService.updateStatus(id, request.getStatus(), recruiter);
        return ResponseEntity.ok(updated);
    }
}
