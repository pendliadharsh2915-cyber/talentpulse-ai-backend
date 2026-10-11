package com.jobportal.service;

import com.jobportal.dto.ResumeAnalysisResponse;
import com.jobportal.entity.Job;
import com.jobportal.entity.Resume;
import com.jobportal.entity.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SkillMatchingService {

    // Common technical skills to look for in resumes and job descriptions
    private static final List<String> KNOWN_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "Spring", "React", "JavaScript", "TypeScript", "Python",
            "SQL", "PostgreSQL", "MySQL", "MongoDB", "HTML", "CSS", "Git", "GitHub",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Linux", "REST API",
            "Microservices", "Hibernate", "JPA", "Node.js", "Express", "C++", "C#",
            ".NET", "Tailwind CSS", "Bootstrap", "Redux", "Kafka", "Redis", "CI/CD",
            "Jenkins", "Agile", "Scrum", "Junit", "Maven", "Gradle"
    );

    private static final Map<String, String> SKILL_ROADMAPS = Map.of(
            "Java", "Deepen Java 21 LTS features (Virtual Threads, Pattern Matching, Records) and JVM performance tuning.",
            "Spring Boot", "Build microservices with Spring Cloud, Spring Security JWT, and production Actuator monitoring.",
            "React", "Master modern React 18 hooks, component modularization, and state synchronization.",
            "Docker", "Practice multi-stage Docker builds, container optimization, and docker-compose orchestration.",
            "Kubernetes", "Deploy resilient clusters using Minikube or K3s; configure Ingress, ConfigMaps, and Helm charts.",
            "AWS", "Gain hands-on experience with core cloud architecture: VPC, EC2, RDS, S3, and IAM security policies.",
            "SQL", "Practice advanced indexing, query execution plans, and relational database schema normalization.",
            "PostgreSQL", "Leverage advanced Postgres features including JSONB querying, indexing, and connection pooling.",
            "Git", "Master Git rebase workflows, branch conflict resolution, and GitHub Actions CI pipelines.",
            "Microservices", "Design distributed systems with asynchronous messaging (Kafka/RabbitMQ) and API gateways."
    );

    /**
     * Extracts skills from free-form text using regex pattern matching.
     */
    public List<String> extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        Set<String> foundSkills = new LinkedHashSet<>();
        for (String skill : KNOWN_SKILLS) {
            String patternString;
            if (skill.contains("+") || skill.contains(".") || skill.contains("#")) {
                patternString = "(?i)\\b" + Pattern.quote(skill) + "(\\b|(?=\\s|$|,|;))";
            } else {
                patternString = "(?i)\\b" + Pattern.quote(skill) + "\\b";
            }

            Pattern pattern = Pattern.compile(patternString);
            if (pattern.matcher(text).find()) {
                foundSkills.add(skill);
            }
        }
        return new ArrayList<>(foundSkills);
    }

    /**
     * Parse comma-separated or space-separated skills string into a clean list of skills
     */
    public List<String> parseSkillList(String skillsString) {
        if (skillsString == null || skillsString.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(skillsString.split("[,;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Analyze a candidate's resume/profile against a specific Job's requirements
     */
    public ResumeAnalysisResponse analyze(Job job, Resume resume, User user) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();

        if (resume != null) {
            response.setResumeId(resume.getId());
            response.setOriginalFilename(resume.getOriginalFilename());
            response.setUploadedAt(resume.getUploadedAt());
        }

        // 1. Gather candidate skills from Resume text + Profile skills fallback
        Set<String> candidateSkills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (resume != null && resume.getExtractedText() != null && !resume.getExtractedText().isBlank()) {
            candidateSkills.addAll(extractSkills(resume.getExtractedText()));
        }
        if (user != null && user.getSkills() != null && !user.getSkills().isBlank()) {
            candidateSkills.addAll(parseSkillList(user.getSkills()));
        }

        // 2. Gather job required skills
        List<String> jobSkills = parseSkillList(job.getRequiredSkills());
        if (jobSkills.isEmpty()) {
            jobSkills = extractSkills(job.getDescription());
        }

        // 3. Match calculation
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String reqSkill : jobSkills) {
            boolean isMatch = candidateSkills.stream()
                    .anyMatch(candSkill -> candSkill.equalsIgnoreCase(reqSkill.trim()));
            if (isMatch) {
                matched.add(reqSkill.trim());
            } else {
                missing.add(reqSkill.trim());
            }
        }

        int totalRequired = jobSkills.size();
        int matchPercentage = 100;
        if (totalRequired > 0) {
            matchPercentage = (int) Math.round(((double) matched.size() / totalRequired) * 100.0);
        }

        // 4. Generate recommendations / suggestions
        List<String> suggestions = new ArrayList<>();
        if (missing.isEmpty()) {
            suggestions.add("Outstanding match! Your profile and resume cover all required skills for this role.");
            suggestions.add("Highlight your most relevant achievements and metric-driven results in your interview.");
        } else {
            suggestions.add("Consider taking a short course or building a project with: " + String.join(", ", missing));
            suggestions.add("Explicitly mention these keywords in your resume and profile if you already have practical experience with them.");
            if (matchPercentage >= 70) {
                suggestions.add("Strong overall profile! You have a competitive advantage with " + matched.size() + " out of " + totalRequired + " required skills.");
            } else if (matchPercentage >= 40) {
                suggestions.add("Moderate alignment. Bridging key gaps in " + missing.get(0) + " will significantly boost your profile visibility.");
            } else {
                suggestions.add("Entry-level alignment for this specific role. Review the job description and align your portfolio.");
            }
        }

        // 5. Practical Skill Roadmap Recommendations
        List<String> skillRecommendations = new ArrayList<>();
        for (String miss : missing) {
            String path = SKILL_ROADMAPS.getOrDefault(miss,
                    "Build an end-to-end practical project incorporating " + miss + " to demonstrate hands-on competence.");
            skillRecommendations.add(miss + ": " + path);
        }

        // 6. Generate AI Executive Summary & Tailored Cover Letter
        String education = (user != null && user.getEducation() != null && !user.getEducation().isBlank())
                ? user.getEducation()
                : "Software Engineering & Computer Science";
        String topSkills = candidateSkills.stream().limit(5).collect(Collectors.joining(", "));
        if (topSkills.isBlank()) {
            topSkills = "Full-Stack Development, Problem Solving, Clean Code Architecture";
        }

        String candidateName = (user != null && user.getName() != null) ? user.getName() : "Applicant";

        String executiveSummary = String.format(
                "Results-driven technology professional with background in %s. Demonstrates core proficiency in %s, with dedicated focus on building scalable, reliable modern systems. Proven aptitude for full-lifecycle delivery, collaborative development, and rapid technical onboarding.",
                education, topSkills
        );

        StringBuilder coverLetter = new StringBuilder();
        coverLetter.append("Dear Hiring Team at ").append(job.getCompany()).append(",\n\n");
        coverLetter.append("I am excited to submit my application for the ").append(job.getTitle()).append(" position. ");
        if (!matched.isEmpty()) {
            coverLetter.append("With verified technical competencies in ")
                    .append(String.join(", ", matched.stream().limit(4).collect(Collectors.toList())))
                    .append(", I am well-prepared to contribute directly to your team's objectives from day one.\n\n");
        } else {
            coverLetter.append("With a solid software engineering background and dedication to building reliable digital solutions, I am eager to apply my skills to your mission.\n\n");
        }
        coverLetter.append("Having reviewed your opening in ").append(job.getLocation())
                .append(", I am impressed by ").append(job.getCompany()).append("'s focus on quality and innovation. ")
                .append("My experience in ").append(education)
                .append(" has given me a rigorous approach to system design, maintainability, and clean code practices.\n\n");

        if (!missing.isEmpty()) {
            coverLetter.append("While actively expanding my depth in ").append(missing.get(0))
                    .append(", my track record of swift adaptation ensures an accelerated ramp-up period.\n\n");
        }

        coverLetter.append("I would appreciate the opportunity to discuss how my background aligns with ").append(job.getCompany()).append("'s goals. Thank you for your time and consideration.\n\n");
        coverLetter.append("Sincerely,\n").append(candidateName);

        response.setDetectedSkills(new ArrayList<>(candidateSkills));
        response.setJobRequiredSkills(jobSkills);
        response.setMatchedSkills(matched);
        response.setMissingSkills(missing);
        response.setMatchPercentage(matchPercentage);
        response.setSuggestions(suggestions);
        response.setSummary("Skill-based algorithmic match: " + matchPercentage + "% (" + matched.size() + "/" + totalRequired + " required skills matched)");
        response.setAiExecutiveSummary(executiveSummary);
        response.setAiCoverLetter(coverLetter.toString());
        response.setSkillRecommendations(skillRecommendations);

        return response;
    }
}
