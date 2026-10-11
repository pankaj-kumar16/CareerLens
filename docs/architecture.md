# CareerLens — Architecture & Security Documentation

## Overview

**CareerLens** is an AI-Powered Job Portal built as a clean **modular monolith**. It provides seamless job searching, resume parsing, ATS-style AI match scoring, application tracking, notifications, and recruiter analytics.

---

## 1. System Architecture

```
                               ┌────────────────────────┐
                               │   Job Seeker / User    │
                               └───────────┬────────────┘
                                           │ HTTPS
                                           ▼
                               ┌────────────────────────┐
                               │ CloudFront + AWS S3    │
                               │  (React + Vite SPA)    │
                               └───────────┬────────────┘
                                           │ API Requests
                                           ▼
                               ┌────────────────────────┐
                               │ AWS Elastic Beanstalk  │
                               │ (Spring Boot Monolith) │
                               └─────┬────────────┬─────┘
                                     │            │
                  ┌──────────────────┘            └──────────────────┐
                  ▼                                                  ▼
      ┌───────────────────────┐                          ┌───────────────────────┐
      │     MongoDB Atlas     │                          │    Private AWS S3     │
      │  (Cloud Database)     │                          │   (Resume Storage)    │
      └───────────────────────┘                          └───────────────────────┘
```

### Architecture Highlights:
1. **Frontend**: React + Vite + Vanilla CSS (No TypeScript). Built with reusable components and responsive design.
2. **Backend**: Java 17+ + Spring Boot 3.x modular monolith with Spring Security.
3. **Database**: MongoDB (Local Compass for development, MongoDB Atlas for production).
4. **File Storage**: Abstracted `FileStorageService` (Local filesystem for dev, Private S3 bucket for production).
5. **Security & Roles**: Strictly enforced dual-role system (`JOB_SEEKER` and `RECRUITER`). No `ADMIN` role.

---

## 2. Authentication & Security Flow

```
User Input (Email) ──► POST /api/auth/register/send-otp (or login/send-otp)
                               │
                               ▼
                    Generate 6-digit OTP ──► Hash OTP ──► Save in Mongo `otp_records`
                               │
                               ▼
                     Send Email via SMTP (or console fallback in dev)
                               │
                               ▼
User Inputs OTP ────► POST /api/auth/verify-otp
                               │
                               ▼
                    Verify OTP Hash & Expiration
                               │
                               ▼
                Issue Signed JWT (HMAC-SHA256) ──► Return AuthResponse DTO
                               │
                               ▼
                Stored in LocalStorage ──► Attached as Bearer Token
```

### Security Guarantees:
- **JWT Signing**: Uses strong `JWT_SECRET` configured via environment variables.
- **Role Enforcement**: Protected routes check user roles in `JwtAuthFilter` and `SecurityConfig`.
- **IDOR Protection**: Business services (`ResumeService`, `JobSeekerService`, `RecruiterService`, `ApplicationService`) verify entity ownership against authenticated `userId` before access.
- **Sanitized Errors**: `GlobalExceptionHandler` masks internal stack traces in production responses.

---

## 3. Resume Storage Abstraction

```
                            ┌─────────────────────┐
                            │ FileStorageService  │ (Interface)
                            └──────────┬──────────┘
                                       │
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
  ┌───────────────────────────┐                 ┌───────────────────────────┐
  │  LocalFileStorageService  │                 │   S3FileStorageService    │
  │  (storage.type=local)     │                 │   (storage.type=s3)       │
  └───────────────────────────┘                 └───────────────────────────┘
```

- **Development**: Files saved locally under `./uploads/resumes/` with path traversal checks (`..` forbidden).
- **Production**: Resumes uploaded directly to private AWS S3 bucket. Objects are non-public.
- **Access**: File downloads are authorized via Spring Boot controller streaming or short-lived presigned URLs.

---

## 4. AI Resume Matching & ATS-Style Scoring

The match score is calculated transparently without hardcoded or random numbers:

```
Total Match Score (0–100%) = 
    (Skill Match Score × 40%) +
    (Experience Match Score × 25%) +
    (Education Match Score × 15%) +
    (Semantic Text Match Score × 20%)
```

### Breakdown:
1. **Skill Match (40%)**: Normalized skill extraction and overlap between resume skills and job required skills.
2. **Experience Match (25%)**: Candidate years of experience compared against required minimum years.
3. **Education Match (15%)**: Alignment of candidate degree level with required education level.
4. **Semantic Similarity (20%)**: Keyword & TF-IDF style semantic similarity between full resume text and job description.

---

## 5. Application & Notification Flow

```
Job Seeker Applies ──► Validate Resume Exists ──► Save Application (Status: APPLIED)
                                                       │
                                                       ▼
                                            Create Notification for Recruiter
                                                       │
                                                       ▼
Recruiter Updates Status (SHORTLISTED/HIRED) ──► Update Application Record
                                                       │
                                                       ▼
                                            Create Notification for Job Seeker
```

---

## 6. Recruiter Analytics Pipeline

Analytics metrics are derived directly from actual database aggregations:
- **Total Jobs**: Count of active/closed jobs created by recruiter.
- **Total Applicants**: Count of distinct applications across recruiter's jobs.
- **Shortlisted Candidates**: Applications marked as `SHORTLISTED` or `HIRED`.
- **Match Distribution**: Categorized breakdown of applicant match scores (High: ≥80%, Medium: 50–79%, Low: <50%).

---

## 7. Production AWS Deployment Plan

### Architecture Components:
- **Frontend**: AWS S3 Static Website Hosting + CloudFront CDN + ACM SSL Certificate.
- **Backend**: AWS Elastic Beanstalk (Java 17 Corretto Platform) or Amazon EC2 behind an ALB.
- **Database**: MongoDB Atlas Cluster with TLS 1.2+ and IP Whitelisting / VPC Peering.
- **File Storage**: AWS S3 Private Bucket with IAM Role-based access control.

### Deployment Checklist:
1. Build frontend: `npm run build` in `/frontend`.
2. Build backend: `mvn clean package` in `/backend` (produces `target/backend-0.0.1-SNAPSHOT.jar`).
3. Set production environment variables in Elastic Beanstalk / EC2 configuration console.
4. Verify health endpoint: `GET /api/health` returns `{"status":"UP"}`.

---

_Document updated: Phase 6 — Production Security, AWS Preparation & Final Cleanup_
