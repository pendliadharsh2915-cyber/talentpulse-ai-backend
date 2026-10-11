package com.jobportal.config;

import com.jobportal.entity.*;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    public DataInitializer(UserRepository userRepository,
                           JobRepository jobRepository,
                           JobApplicationRepository jobApplicationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData) {
            log.info("Demo data seeding is disabled.");
            return;
        }

        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping demo seed.");
            return;
        }

        log.info("Seeding initial demo users and realistic job listings...");

        // 1. Create Demo Recruiter
        User recruiter = new User();
        recruiter.setName("Sarah Jenkins");
        recruiter.setEmail("recruiter@demo.com");
        recruiter.setPasswordHash(passwordEncoder.encode("password123"));
        recruiter.setRole(Role.RECRUITER);
        recruiter.setPhone("+1-555-0144");
        recruiter.setEducation("M.S. in Human Resource Management, Stanford");
        recruiter.setSkills("Talent Acquisition, Technical Recruiting, Leadership");
        recruiter = userRepository.save(recruiter);

        // 2. Create Demo Job Seeker
        User seeker = new User();
        seeker.setName("Alex Morgan");
        seeker.setEmail("seeker@demo.com");
        seeker.setPasswordHash(passwordEncoder.encode("password123"));
        seeker.setRole(Role.JOB_SEEKER);
        seeker.setPhone("+1-555-0199");
        seeker.setEducation("B.S. in Computer Science, University of Washington");
        seeker.setSkills("Java, Spring Boot, React, JavaScript, SQL, HTML, CSS, Git");
        seeker = userRepository.save(seeker);

        // 3. Create 10 Sample Jobs across diverse tech specializations
        List<Job> sampleJobs = List.of(
            new Job(null,
                "Senior Full-Stack Java Developer",
                "CloudScale Solutions",
                "Join our core platform engineering team to build scalable cloud-native microservices and responsive web applications. You will architect robust backends using Java 21 & Spring Boot, while collaborating on modern React frontend interfaces.",
                "Remote, US",
                "Full-time",
                "$130,000 - $160,000",
                "Java, Spring Boot, React, PostgreSQL, Docker, Microservices, Git",
                true, recruiter, LocalDateTime.now().minusDays(3)),

            new Job(null,
                "Frontend React Engineer",
                "PixelCraft Studio",
                "We are seeking a talented React developer passionate about crafting pixel-perfect, accessible user experiences. You will lead UI component architecture, state management with modern hooks, and responsive design systems.",
                "San Francisco, CA / Hybrid",
                "Full-time",
                "$115,000 - $140,000",
                "React, JavaScript, TypeScript, HTML, CSS, Tailwind CSS, Redux",
                true, recruiter, LocalDateTime.now().minusDays(5)),

            new Job(null,
                "Backend Java Engineer (High Throughput)",
                "FinTech Vanguard",
                "Help power our next-generation transactional ledger. We require deep experience with concurrency, high-performance REST APIs, event-driven architectures, and distributed caching in Java.",
                "New York, NY",
                "Full-time",
                "$125,000 - $155,000",
                "Java, Spring Boot, Kafka, Redis, SQL, Microservices, AWS",
                true, recruiter, LocalDateTime.now().minusDays(2)),

            new Job(null,
                "AI / ML Engineer",
                "Cognitive Intelligence Labs",
                "Develop and deploy intelligent machine learning pipelines and NLP models into production. Build data preprocessing stages, automated model evaluation, and performant REST inference services.",
                "Boston, MA / Hybrid",
                "Full-time",
                "$145,000 - $180,000",
                "Python, Docker, SQL, Git, REST API, Linux",
                true, recruiter, LocalDateTime.now().minusDays(4)),

            new Job(null,
                "Data Analyst & BI Specialist",
                "Insight Metrics",
                "Translate complex business datasets into actionable insights and executive dashboards. Write performant SQL queries, automate reporting ETL pipelines with Python, and collaborate with business leaders.",
                "Chicago, IL",
                "Full-time",
                "$85,000 - $110,000",
                "SQL, Python, PostgreSQL, Git",
                true, recruiter, LocalDateTime.now().minusDays(7)),

            new Job(null,
                "DevOps & Cloud Engineer",
                "Nexus Infrastructure",
                "Automate CI/CD pipelines, manage Kubernetes clusters on AWS, and establish observability across distributed microservices. Drive infrastructure-as-code and high availability best practices.",
                "Remote",
                "Full-time",
                "$130,000 - $165,000",
                "Docker, Kubernetes, AWS, CI/CD, Jenkins, Linux, Git",
                true, recruiter, LocalDateTime.now().minusDays(1)),

            new Job(null,
                "Junior Java Software Developer",
                "Apex Enterprise Systems",
                "Great opportunity for emerging developers to level up their careers! You will learn our enterprise codebases, develop new features in Spring Boot, write automated tests, and collaborate with experienced mentors.",
                "Austin, TX",
                "Full-time",
                "$75,000 - $95,000",
                "Java, Spring Boot, SQL, Git, HTML, CSS",
                true, recruiter, LocalDateTime.now().minusDays(6)),

            new Job(null,
                "UI/UX & Web Developer",
                "Modern Wave Media",
                "Design and implement responsive, high-converting web portals and interactive brand applications. Strong aesthetic eye and frontend HTML/CSS/React proficiency required.",
                "Seattle, WA",
                "Contract",
                "$90,000 - $110,000",
                "React, JavaScript, HTML, CSS, Tailwind CSS",
                true, recruiter, LocalDateTime.now().minusDays(8)),

            new Job(null,
                "Data Engineer",
                "DataStream Analytics",
                "Architect resilient real-time and batch data pipelines. Work with distributed streaming engines, relational and non-relational datastores, and cloud storage architectures.",
                "Denver, CO / Remote",
                "Full-time",
                "$120,000 - $150,000",
                "Python, SQL, PostgreSQL, Kafka, Docker, Git",
                true, recruiter, LocalDateTime.now().minusDays(9)),

            new Job(null,
                "Full-Stack Software Engineer (Internship)",
                "Startup Accelerate",
                "Work closely with our founders to build rapid prototypes and test user features across web and mobile. Perfect for students and recent grads seeking hands-on full stack experience.",
                "Remote",
                "Internship",
                "$35/hr",
                "JavaScript, React, Java, HTML, CSS, Git",
                true, recruiter, LocalDateTime.now().minusDays(10))
        );

        List<Job> savedJobs = jobRepository.saveAll(sampleJobs);

        // Seed 1 initial application for demo seeker to illustrate application history
        Job firstJob = savedJobs.get(0);
        JobApplication initialApplication = new JobApplication();
        initialApplication.setJob(firstJob);
        initialApplication.setJobSeeker(seeker);
        initialApplication.setStatus(ApplicationStatus.REVIEWING);
        initialApplication.setAppliedAt(LocalDateTime.now().minusDays(1));
        jobApplicationRepository.save(initialApplication);

        log.info("Demo data seeding completed successfully! Seeded 2 users, {} jobs, and 1 application.", savedJobs.size());
    }
}
