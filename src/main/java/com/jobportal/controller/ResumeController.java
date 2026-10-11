package com.jobportal.controller;

import com.jobportal.dto.ResumeAnalysisResponse;
import com.jobportal.entity.Resume;
import com.jobportal.entity.User;
import com.jobportal.service.ResumeService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        Resume resume = resumeService.uploadResume(file, user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("resumeId", resume.getId());
        resp.put("filename", resume.getOriginalFilename());
        resp.put("uploadedAt", resume.getUploadedAt());
        resp.put("message", "Resume uploaded and processed successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyResume(@AuthenticationPrincipal User user) {
        Resume resume = resumeService.getLatestResume(user);
        if (resume == null) {
            return ResponseEntity.ok(Map.of("hasResume", false));
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("hasResume", true);
        resp.put("resumeId", resume.getId());
        resp.put("filename", resume.getOriginalFilename());
        resp.put("uploadedAt", resume.getUploadedAt());
        resp.put("textPreview", resume.getExtractedText() != null && resume.getExtractedText().length() > 300
                ? resume.getExtractedText().substring(0, 300) + "..."
                : resume.getExtractedText());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/analysis/{jobId}")
    public ResponseEntity<ResumeAnalysisResponse> analyzeForJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user
    ) {
        ResumeAnalysisResponse analysis = resumeService.analyzeResumeForJob(jobId, user);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/candidate/{resumeId}")
    public ResponseEntity<Map<String, Object>> getCandidateResume(
            @PathVariable Long resumeId
    ) {
        Resume resume = resumeService.getResumeById(resumeId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("resumeId", resume.getId());
        resp.put("filename", resume.getOriginalFilename());
        resp.put("uploadedAt", resume.getUploadedAt());
        resp.put("extractedText", resume.getExtractedText());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        Resume resume = resumeService.getResumeById(id);
        Resource resource = resumeService.downloadResume(id, user);

        String filename = resume.getOriginalFilename() != null ? resume.getOriginalFilename() : "resume.pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
