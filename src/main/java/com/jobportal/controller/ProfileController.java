package com.jobportal.controller;

import com.jobportal.dto.ProfileUpdateRequest;
import com.jobportal.dto.UserDto;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    public ProfileController(UserService userService, JobService jobService, ApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal User currentUser) {
        UserDto profile = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        UserDto updated = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = new HashMap<>();
        long totalJobs = jobService.getTotalActiveJobsCount();
        stats.put("totalActiveJobs", totalJobs);

        if (currentUser.getRole() == Role.JOB_SEEKER) {
            long myApplications = applicationService.getMyApplicationsCount(currentUser.getId());
            stats.put("myApplicationsCount", myApplications);
        } else if (currentUser.getRole() == Role.RECRUITER) {
            long myJobs = jobService.getRecruiterJobCount(currentUser.getId());
            long receivedApplications = applicationService.getRecruiterApplicationsCount(currentUser.getId());
            stats.put("myJobsCount", myJobs);
            stats.put("receivedApplicationsCount", receivedApplications);
        }

        return ResponseEntity.ok(stats);
    }
}
