package com.jobportal.service;

import com.jobportal.dto.ResumeAnalysisResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.Resume;
import com.jobportal.entity.Role;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.ResumeRepository;
import com.jobportal.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SkillMatchingService skillMatchingService;

    @Value("${app.upload.dir:./uploads/resumes}")
    private String uploadDir;

    public ResumeService(ResumeRepository resumeRepository,
                         JobRepository jobRepository,
                         UserRepository userRepository,
                         JobApplicationRepository jobApplicationRepository,
                         SkillMatchingService skillMatchingService) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.skillMatchingService = skillMatchingService;
    }

    @Transactional
    public Resume uploadResume(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a PDF resume file to upload");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF (.pdf) documents are supported");
        }

        // Validate max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 5MB limit");
        }

        // 1. Extract text from PDF using PDFBox
        String extractedText = "";
        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
        } catch (Exception e) {
            extractedText = "Extraction note: " + e.getMessage();
        }

        // 2. Safely store the file
        String storedFilename = UUID.randomUUID() + ".pdf";
        try {
            Path storageDirectory = Paths.get(uploadDir);
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
            Path targetPath = storageDirectory.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store resume file on server: " + e.getMessage());
        }

        // 3. Save Resume record
        Resume resume = new Resume();
        resume.setJobSeeker(user);
        resume.setOriginalFilename(originalFilename);
        resume.setStoredFilename(storedFilename);
        resume.setExtractedText(extractedText);
        resume.setUploadedAt(LocalDateTime.now());
        Resume saved = resumeRepository.save(resume);

        // 4. Also auto-populate User's skills if user doesn't have any skills configured
        if (extractedText != null && !extractedText.isBlank()) {
            List<String> detected = skillMatchingService.extractSkills(extractedText);
            if (!detected.isEmpty()) {
                if (user.getSkills() == null || user.getSkills().isBlank()) {
                    user.setSkills(String.join(", ", detected));
                    userRepository.save(user);
                }
            }
        }

        return saved;
    }

    public Resume getLatestResume(User user) {
        return resumeRepository.findFirstByJobSeekerIdOrderByUploadedAtDesc(user.getId())
                .orElse(null);
    }

    public ResumeAnalysisResponse analyzeResumeForJob(Long jobId, User user) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        Resume resume = getLatestResume(user);
        return skillMatchingService.analyze(job, resume, user);
    }

    public Resume getResumeById(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + id));
    }

    public Resource downloadResume(Long resumeId, User currentUser) {
        Resume resume = getResumeById(resumeId);

        // Security authorization check
        if (currentUser.getRole() == Role.JOB_SEEKER) {
            if (!resume.getJobSeeker().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to download this candidate resume");
            }
        } else if (currentUser.getRole() == Role.RECRUITER) {
            boolean hasApplied = jobApplicationRepository.existsByJobSeekerIdAndJobRecruiterId(
                    resume.getJobSeeker().getId(), currentUser.getId());
            if (!hasApplied) {
                throw new AccessDeniedException("You can only download resumes for candidates who applied to your requisitions");
            }
        }

        Path targetPath = Paths.get(uploadDir).resolve(resume.getStoredFilename());
        if (Files.exists(targetPath)) {
            try {
                Resource resource = new UrlResource(targetPath.toUri());
                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
            } catch (MalformedURLException ignored) {
            }
        }

        // Resilient fallback PDF using PDFBox
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA_BOLD, 15);
                stream.newLineAtOffset(50, 750);
                stream.showText("Candidate Resume: " + resume.getJobSeeker().getName());
                stream.newLineAtOffset(0, -20);
                stream.setFont(PDType1Font.HELVETICA, 10);
                stream.showText("Email: " + resume.getJobSeeker().getEmail() + " | File: " + resume.getOriginalFilename());
                stream.newLineAtOffset(0, -25);
                stream.setFont(PDType1Font.HELVETICA, 9);

                String text = resume.getExtractedText() != null ? resume.getExtractedText() : "Extracted document text.";
                String[] lines = text.split("[\\r\\n]+");
                int count = 0;
                for (String l : lines) {
                    if (count++ > 40) break;
                    String sanitized = l.replaceAll("[^\\x20-\\x7E]", "").trim();
                    if (!sanitized.isEmpty()) {
                        if (sanitized.length() > 85) sanitized = sanitized.substring(0, 85);
                        stream.showText(sanitized);
                        stream.newLineAtOffset(0, -13);
                    }
                }
                stream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException("Could not generate resume document: " + ex.getMessage());
        }
    }
}
