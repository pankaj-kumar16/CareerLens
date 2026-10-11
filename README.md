# CareerLens — Job Portal with AI-Based Resume Matching

> **"See your career through a smarter lens."**

**CareerLens** is a modern, modular monolith web application designed for **Job Seekers** and **Recruiters**. It combines passwordless OTP authentication, automated resume parsing, ATS-style AI resume-job match scoring, job search & application management, notifications, recruiter candidate pipelines, and real-time recruiter analytics.

---

## 🏗️ Technology Stack

- **Frontend**: React + Vite + Vanilla CSS / Tailwind CSS / shadcn/ui components (JavaScript / JSX only, NO TypeScript)
- **Backend**: Java 17+ + Spring Boot 3.x (Modular Monolith architecture with Spring Security & JWT)
- **Database**: MongoDB (Local Compass for development, MongoDB Atlas for production)
- **AI Engine**: Hybrid semantic + structured match engine (Skill overlap, Experience, Education, and Keyword TF-IDF semantic scoring)
- **Storage**: Abstracted `FileStorageService` (Local filesystem for dev, Private AWS S3 for production)
- **Deployment Preparation**: AWS Elastic Beanstalk (Backend), AWS S3 + CloudFront (Frontend), MongoDB Atlas (Database)

---

## 🔒 Security & Roles

CareerLens enforces strict access control rules:
- **Roles**: Only `JOB_SEEKER` and `RECRUITER`. There is **NO ADMIN** role.
- **Authentication**: Email OTP verification (6-digit hashed OTPs with 10-minute expiration).
- **JWT Authorization**: HMAC-SHA256 tokens issued on login, validated on protected routes via `JwtAuthFilter`.
- **IDOR Protection**: Endpoint authorization verifies ownership before serving user profiles, resumes, applications, jobs, notifications, or recruiter analytics.
- **File Upload Security**: Strict MIME type validation (PDF and DOCX only), 5MB file size limit, path traversal prevention (`..` sanitization), non-executable storage, and non-public resume access.
- **Error Handling**: Centralized `GlobalExceptionHandler` masks internal stack traces and database details in production API responses.
- **Environment Secrets**: Credentials and API keys managed strictly through environment variables. Zero hardcoded secrets in source code or committed files.

---

## 🏛️ System Architecture

```
User (Browser)
   │
   ├──► CloudFront + S3 (React Frontend SPA)
   │
   └──► HTTPS API Requests
           │
           ▼
     AWS Elastic Beanstalk / EC2 (Spring Boot Monolith)
           │
           ├──► MongoDB Atlas (Production DB)
           │
           └──► Private AWS S3 (Private Resume Files & Presigned Access)
```

For complete architecture diagrams and service maps, see [docs/architecture.md](file:///d:/PROJECT/CareerLens/docs/architecture.md).

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env` to configure local or production environment variables:

```env
# Application Configuration
APP_BASE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:5173

# Database
MONGODB_URI=mongodb://localhost:27017/careerlens
MONGODB_DATABASE=careerlens

# JWT Authentication
JWT_SECRET=REPLACE_WITH_STRONG_RANDOM_SECRET_KEY_MIN_256_BITS
JWT_EXPIRATION_MS=86400000

# CORS Allowed Origins
CORS_ALLOWED_ORIGINS=http://localhost:5173

# Email Service (SMTP)
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
EMAIL_FROM=your-email@gmail.com

# AI Integration
AI_PROVIDER=openai
AI_MODEL=gpt-3.5-turbo
AI_API_KEY=your-ai-api-key-here

# Storage Abstraction
STORAGE_TYPE=local
LOCAL_UPLOAD_PATH=./uploads

# AWS S3 Resume Storage (Production)
AWS_REGION=us-east-1
AWS_S3_BUCKET=careerlens-resumes-private
AWS_ACCESS_KEY_ID=your-aws-access-key-id
AWS_SECRET_ACCESS_KEY=your-aws-secret-access-key

# Frontend Environment Variables (Vite)
VITE_API_BASE_URL=http://localhost:8080
```

---

## 🛠️ Local Development Setup

### 1. Prerequisites
- **Node.js** (v18+)
- **Java JDK** 17+
- **Apache Maven** 3.8+
- **MongoDB** (Local instance running on `mongodb://localhost:27017`)

### 2. Backend Setup

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

The Spring Boot API will start at `http://localhost:8080`.
Health endpoint check: `GET http://localhost:8080/api/health` $\rightarrow$ `{"status":"UP","application":"CareerLens"}`.

### 3. Frontend Setup

```bash
cd frontend
npm install
npm run build
npm run dev
```

The React Vite application will open at `http://localhost:5173`.

---

## 📡 Core API Endpoints

### Health & Auth
- `GET /api/health` — Application health check
- `POST /api/auth/register/send-otp` — Send registration OTP
- `POST /api/auth/register/verify-otp` — Verify OTP & complete registration
- `POST /api/auth/login/send-otp` — Send login OTP
- `POST /api/auth/login/verify-otp` — Verify OTP & issue JWT
- `GET /api/auth/me` — Authenticated user details

### Job Seeker (`JOB_SEEKER` role)
- `GET/PUT /api/job-seeker/profile` — Profile management
- `POST /api/job-seeker/resume` — Upload PDF/DOCX resume
- `GET /api/job-seeker/resume` — View parsed resume
- `GET /api/job-seeker/resume/download` — Download resume file
- `GET /api/job-seeker/resume/analysis` — View ATS-style AI score & breakdown
- `GET /api/job-seeker/recommendations` — AI personalized job recommendations
- `POST /api/jobs/{id}/apply` — Apply for a job
- `GET /api/job-seeker/applications` — Track application status

### Recruiter (`RECRUITER` role)
- `GET/PUT /api/recruiter/profile` — Recruiter profile & company details
- `POST /api/recruiter/jobs` — Post a new job
- `GET /api/recruiter/jobs` — Manage recruiter job listings
- `GET /api/recruiter/jobs/{jobId}/candidates` — AI-ranked candidate list for job
- `PUT /api/recruiter/applications/{id}/status` — Pipeline status transitions
- `GET /api/recruiter/analytics` — Recruiter analytics & applicant metrics

### Notifications
- `GET /api/notifications` — View user notifications
- `GET /api/notifications/unread-count` — Unread count for UI badges
- `PUT /api/notifications/{id}/read` — Mark notification as read
- `PUT /api/notifications/read-all` — Mark all notifications as read

---

## 🚀 AWS Production Deployment Preparation

1. **Backend Packaging**:
   `mvn clean package` generates `target/backend-0.0.1-SNAPSHOT.jar` ready for AWS Elastic Beanstalk / EC2.
2. **Frontend Build**:
   `npm run build` generates static assets in `frontend/dist/` ready for AWS S3 & CloudFront CDN deployment.
3. **MongoDB Atlas**:
   Configure production connection string in `MONGODB_URI` environment variable.
4. **Private Resume Storage**:
   Set `STORAGE_TYPE=s3`, `AWS_S3_BUCKET`, `AWS_REGION`, and AWS IAM access credentials in environment configuration.

---

## 🧪 Build & Test Verification Status

- **Backend Unit & Integration Tests**: 59 tests passing (`mvn clean test`).
- **Backend Executable JAR**: Successfully created (`mvn clean package`).
- **Frontend Production Build**: Successfully compiled (`npm run build`).
- **Health Endpoint**: Verified `GET /api/health` returning `{"status":"UP"}`.

---

## 📜 License

Academic Project — B.Tech Final Year.
