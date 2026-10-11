package com.jobportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeAnalysisResponse {
    private Long resumeId;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private List<String> detectedSkills;
    private List<String> jobRequiredSkills;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private int matchPercentage;
    private List<String> suggestions;
    private String summary;
    private String aiExecutiveSummary;
    private String aiCoverLetter;
    private List<String> skillRecommendations;

    public ResumeAnalysisResponse() {}

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public List<String> getDetectedSkills() {
        return detectedSkills;
    }

    public void setDetectedSkills(List<String> detectedSkills) {
        this.detectedSkills = detectedSkills;
    }

    public List<String> getJobRequiredSkills() {
        return jobRequiredSkills;
    }

    public void setJobRequiredSkills(List<String> jobRequiredSkills) {
        this.jobRequiredSkills = jobRequiredSkills;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public int getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(int matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAiExecutiveSummary() {
        return aiExecutiveSummary;
    }

    public void setAiExecutiveSummary(String aiExecutiveSummary) {
        this.aiExecutiveSummary = aiExecutiveSummary;
    }

    public String getAiCoverLetter() {
        return aiCoverLetter;
    }

    public void setAiCoverLetter(String aiCoverLetter) {
        this.aiCoverLetter = aiCoverLetter;
    }

    public List<String> getSkillRecommendations() {
        return skillRecommendations;
    }

    public void setSkillRecommendations(List<String> skillRecommendations) {
        this.skillRecommendations = skillRecommendations;
    }
}
