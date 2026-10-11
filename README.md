# TalentPulse AI — Spring Boot Backend & Resume Intelligence Engine

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/pendliadharsh2915-cyber/talentpulse-ai-backend)

The core backend service for **TalentPulse AI**, an enterprise-grade job portal and Resume Intelligence platform. Powered by **Java 21**, **Spring Boot 3.3.4**, **Spring Security**, **Spring Data JPA**, **Apache PDFBox**, and **H2 / PostgreSQL**.

---

## 🌟 Architecture & Capabilities

* **Stateless JWT Authentication**: Secure HMAC-SHA256 authentication with role-based authorization (`JOB_SEEKER` vs `RECRUITER`) and BCrypt password hashing.
* **Resume Intelligence Engine**:
  * Upload validation (<5MB, PDF format).
  * Automated document text extraction using **Apache PDFBox 2.0.31**.
  * Rule-based skill extraction detecting 40+ technologies.
  * Transparent match percentage scoring comparing candidate competencies against job requirements.
  * AI-generated **Executive Summary** and **Tailored Cover Letter**.
  * Practical skill gap learning roadmaps.
* **Exact Resume Snapshot Freezing**: Links each `JobApplication` to the exact version of the resume submitted, guaranteeing that future resume uploads do not alter historical application records.
* **Secure PDF Streaming**: Authenticated recruiters can download original PDF resumes directly via `/api/resumes/{id}/download` with dynamic fallback regeneration.
* **Persistent In-App Notifications**: Automatically dispatches alerts when candidates apply and when recruiters update application status (`APPLIED` ➔ `REVIEWING` ➔ `SHORTLISTED` ➔ `REJECTED`).
* **Advanced Requisition Search & Pagination**: Spring Data JPA query filtering by keyword, employment type, and work mode with full `Pageable` pagination.
* **Saved Jobs (Bookmarks)**: 1-click requisition saving and retrieval.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Runtime & Language** | Java 21 LTS |
| **Framework** | Spring Boot 3.3.4 (Spring Web, Spring Validation) |
| **Security** | Spring Security 6, JJWT 0.12.6 (HMAC-SHA256), BCrypt |
| **Persistence** | Spring Data JPA, Hibernate ORM 6.5 |
| **Database** | H2 File DB (Dev Profile) / PostgreSQL 16 (Prod Profile) |
| **Document Processing**| Apache PDFBox 2.0.31 |
| **Build Tool** | Apache Maven 3.9+ |

---

## 🚀 Quick Start (Local Development)

### Prerequisites
* **Java 21 JDK** installed (`java -version`)
* **Apache Maven 3.8+** installed (`mvn -version`)

### 1. Run the Application (Dev Profile)
```bash
mvn spring-boot:run
```
* The server will start on port **`8080`**.
* Base API URL: **`http://localhost:8080/api`**
* Health Check: **`http://localhost:8080/api/health`**
* H2 Database Console: **`http://localhost:8080/h2-console`**
  * JDBC URL: `jdbc:h2:file:./data/jobportaldb`
  * Username: `sa`
  * Password: *(empty)*

*Note: In the `dev` profile, demo tech positions and test accounts are automatically seeded into the database on first run.*

### 2. Run Tests
```bash
mvn test
```

---

## 🔑 Pre-Configured Demo Accounts

| Role | Email | Password | Pre-Loaded Data |
|---|---|---|---|
| **Job Seeker** | `seeker@demo.com` | `password123` | B.S. in Computer Science, 8 technical skills |
| **Recruiter** | `recruiter@demo.com` | `password123` | Owner of 10 active tech requisitions |

---

## 📡 REST API Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new candidate or recruiter |
| `POST` | `/api/auth/login` | Public | Authenticate credentials and receive JWT |
| `GET` | `/api/auth/me` | Authenticated | Retrieve current user profile |

### Jobs (`/api/jobs`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/jobs` | Public | Search and browse active jobs (supports `?search=`, `?employmentType=`, `?location=`, `?page=`, `?size=`) |
| `GET` | `/api/jobs/{id}` | Public | Retrieve job details and requirements |
| `POST` | `/api/jobs` | Recruiter | Post a new job opening |
| `PUT` | `/api/jobs/{id}` | Recruiter | Update owned job opening |
| `DELETE`| `/api/jobs/{id}` | Recruiter | Deactivate owned job opening |
| `POST` | `/api/jobs/{id}/save` | Job Seeker | Toggle bookmark/saved status for job |
| `GET` | `/api/jobs/saved` | Job Seeker | List all saved/bookmarked jobs |

### Applications (`/api/applications` & `/api/jobs`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/jobs/{jobId}/apply` | Job Seeker | Submit application (freezes resume snapshot) |
| `GET` | `/api/applications/my` | Job Seeker | View candidate applications |
| `GET` | `/api/recruiter/jobs/{jobId}/applications` | Recruiter | View applications for specific job |
| `GET` | `/api/recruiter/applications` | Recruiter | View all applications across owned jobs |
| `PATCH`| `/api/applications/{id}/status` | Recruiter | Advance status (`APPLIED`, `REVIEWING`, `SHORTLISTED`, `REJECTED`) |

### Resume Intelligence (`/api/resumes`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/resumes/upload` | Job Seeker | Upload PDF resume, parse text via PDFBox |
| `GET` | `/api/resumes/my` | Job Seeker | Retrieve latest uploaded resume status |
| `GET` | `/api/resumes/analysis/{jobId}` | Job Seeker | Algorithmic match analysis, AI summary & cover letter |
| `GET` | `/api/resumes/candidate/{resumeId}` | Recruiter | View extracted text for candidate |
| `GET` | `/api/resumes/{id}/download` | Authenticated | Securely stream candidate PDF resume file |

### Notifications (`/api/notifications`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/notifications` | Authenticated | List user notifications |
| `GET` | `/api/notifications/unread-count` | Authenticated | Count unread notifications |
| `PATCH`| `/api/notifications/{id}/read` | Authenticated | Mark notification as read |
| `PATCH`| `/api/notifications/read-all` | Authenticated | Mark all notifications as read |

---

## ⚙️ Production PostgreSQL Deployment

Set the following environment variables:
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<database>
SPRING_DATASOURCE_USERNAME=<db_user>
SPRING_DATASOURCE_PASSWORD=<db_password>
APP_JWT_SECRET=<256_bit_hex_secret>
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

Build the production JAR:
```bash
mvn clean package -DskipTests
java -jar target/ai-job-portal-1.0.0.jar
```

---

## 📄 License
MIT License.
