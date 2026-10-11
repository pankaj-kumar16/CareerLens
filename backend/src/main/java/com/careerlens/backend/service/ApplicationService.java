package com.careerlens.backend.service;

import com.careerlens.backend.dto.ApplicationResponse;
import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.dto.ResumeResponse;
import com.careerlens.backend.dto.UserResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.*;
import com.careerlens.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobService jobService;
    private final NotificationService notificationService;
    private final ResumeMatchingService resumeMatchingService;

    public ApplicationService(ApplicationRepository applicationRepository,
                               JobRepository jobRepository,
                               ResumeRepository resumeRepository,
                               UserRepository userRepository,
                               CompanyRepository companyRepository,
                               JobService jobService,
                               NotificationService notificationService,
                               ResumeMatchingService resumeMatchingService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobService = jobService;
        this.notificationService = notificationService;
        this.resumeMatchingService = resumeMatchingService;
    }

    public ApplicationResponse applyForJob(String jobSeekerId, String jobId, String resumeId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));

        if (!job.isActive()) {
            throw new BadRequestException("This job posting is no longer active.");
        }

        Resume resume;
        if (org.springframework.util.StringUtils.hasText(resumeId)) {
            resume = resumeRepository.findByIdAndJobSeekerId(resumeId, jobSeekerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Selected resume version not found or access denied."));
        } else {
            resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                    .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId).stream().findFirst())
                    .orElseThrow(() -> new BadRequestException("You must upload a resume before applying for jobs."));
        }

        Optional<Application> existingOpt = applicationRepository.findByJobIdAndJobSeekerId(jobId, jobSeekerId);
        if (existingOpt.isPresent() && (existingOpt.get().getId() == null || !existingOpt.get().getId().startsWith("discovery_"))) {
            throw new BadRequestException("You have already applied for this job.");
        }

        MatchResult matchResult = null;
        try {
            matchResult = resumeMatchingService.calculateMatch(resume, job);
        } catch (Exception ignored) {}

        ApplicationStatus initialStatus = ApplicationStatus.APPLIED;
        if (matchResult != null && matchResult.getOverallScore() >= 90) {
            initialStatus = ApplicationStatus.SHORTLISTED;
        }

        Application application = new Application(
                null,
                jobId,
                jobSeekerId,
                resume.getId(),
                initialStatus,
                Instant.now(),
                Instant.now(),
                null,
                null
        );

        Application saved = applicationRepository.save(application);
        String companyName = resolveCompanyName(job);

        // Notify job seeker
        try {
            if (initialStatus == ApplicationStatus.SHORTLISTED) {
                notificationService.createNotification(
                        jobSeekerId,
                        NotificationType.SHORTLISTED,
                        "Application Auto-Shortlisted!",
                        "Great news! Your resume scored a " + matchResult.getOverallScore() + "% match for " + job.getTitle() + " at " + companyName + " and was automatically shortlisted!",
                        saved.getId()
                );
            } else {
                notificationService.createNotification(
                        jobSeekerId,
                        NotificationType.APPLICATION_SUBMITTED,
                        "Application Submitted",
                        "You have successfully applied for position: " + job.getTitle() + " at " + companyName + ".",
                        saved.getId()
                );
            }
        } catch (Exception ignored) {}

        return mapToResponse(saved);
    }

    public ApplicationResponse applyForJob(String jobSeekerId, String jobId) {
        return applyForJob(jobSeekerId, jobId, null);
    }

    public List<ApplicationResponse> getApplicationsForJobSeeker(String jobSeekerId) {
        List<Application> applications = applicationRepository.findByJobSeekerIdOrderByAppliedAtDesc(jobSeekerId);
        return applications.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ApplicationResponse getApplicationByIdForJobSeeker(String applicationId, String jobSeekerId) {
        Application application = applicationRepository.findByIdAndJobSeekerId(applicationId, jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found or access denied."));
        return mapToResponse(application);
    }

    public List<ApplicationResponse> getApplicationsForRecruiterJob(String jobId, String recruiterId) {
        Job job = jobRepository.findByIdAndRecruiterId(jobId, recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied."));

        List<Application> applications = applicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId());
        for (Application app : applications) {
            if (app.getStatus() == ApplicationStatus.APPLIED) {
                try {
                    MatchResult match = resumeMatchingService.matchResumeToJob(app.getResumeId(), jobId);
                    if (match != null && match.getOverallScore() >= 90) {
                        app.setStatus(ApplicationStatus.SHORTLISTED);
                        app.setUpdatedAt(Instant.now());
                        applicationRepository.save(app);
                    }
                } catch (Exception ignored) {}
            }
        }
        return applications.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ApplicationResponse updateApplicationStatus(String applicationId, String recruiterId,
                                                        ApplicationStatus status, Instant interviewDate) {
        Application application = applicationRepository.findById(applicationId).orElse(null);

        if (application == null && applicationId != null && applicationId.startsWith("discovery_")) {
            String[] parts = applicationId.split("_");
            if (parts.length >= 3) {
                String seekerId = parts[1];
                String jobId = parts[2];

                Resume resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(seekerId)
                        .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(seekerId).stream().findFirst())
                        .orElse(null);

                application = new Application(
                        null,
                        jobId,
                        seekerId,
                        resume != null ? resume.getId() : null,
                        ApplicationStatus.APPLIED,
                        Instant.now(),
                        Instant.now(),
                        null,
                        null
                );
            }
        }

        if (application == null) {
            throw new ResourceNotFoundException("Application not found.");
        }

        Job job = jobRepository.findByIdAndRecruiterId(application.getJobId(), recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied for this application."));

        validateStatusTransition(application.getStatus(), status);

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(status);
        if (interviewDate != null) {
            application.setInterviewDate(interviewDate);
        }
        application.setUpdatedAt(Instant.now());

        Application updated = applicationRepository.save(application);
        String companyName = resolveCompanyName(job);

        // Send notification to candidate if status or interview date changed
        try {
            NotificationType type = switch (status) {
                case SHORTLISTED -> NotificationType.SHORTLISTED;
                case INTERVIEW -> NotificationType.INTERVIEW;
                case SELECTED -> NotificationType.SELECTED;
                case REJECTED -> NotificationType.REJECTED;
                default -> NotificationType.APPLICATION_STATUS_CHANGED;
            };

            String title = switch (status) {
                case SHORTLISTED -> "Application Shortlisted!";
                case INTERVIEW -> "Interview Scheduled!";
                case SELECTED -> "Congratulations! You are Selected!";
                case REJECTED -> "Application Status Update";
                default -> "Application Status Updated";
            };

            String message = "Your application for " + job.getTitle() + " at " + companyName +
                    " has been updated to " + status + ".";
            if (interviewDate != null && status == ApplicationStatus.INTERVIEW) {
                message = "Interview scheduled for " + job.getTitle() + " at " + companyName + ".";
            }

            notificationService.createNotification(application.getJobSeekerId(), type, title, message, updated.getId());
        } catch (Exception ignored) {}

        return mapToResponse(updated);
    }

    public ApplicationResponse updateApplicationNotes(String applicationId, String recruiterId, String recruiterNotes) {
        Application application = applicationRepository.findById(applicationId).orElse(null);

        if (application == null && applicationId != null && applicationId.startsWith("discovery_")) {
            String[] parts = applicationId.split("_");
            if (parts.length >= 3) {
                String seekerId = parts[1];
                String jobId = parts[2];

                Resume resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(seekerId)
                        .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(seekerId).stream().findFirst())
                        .orElse(null);

                application = new Application(
                        null,
                        jobId,
                        seekerId,
                        resume != null ? resume.getId() : null,
                        ApplicationStatus.APPLIED,
                        Instant.now(),
                        Instant.now(),
                        null,
                        null
                );
            }
        }

        if (application == null) {
            throw new ResourceNotFoundException("Application not found.");
        }

        jobRepository.findByIdAndRecruiterId(application.getJobId(), recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied for this application."));

        if (StringUtils.hasText(recruiterNotes)) {
            application.setRecruiterNotes(recruiterNotes);
        }
        application.setUpdatedAt(Instant.now());

        Application updated = applicationRepository.save(application);
        return mapToResponse(updated);
    }

    public List<ApplicationResponse> getShortlistedCandidatesForRecruiter(String recruiterId) {
        List<Job> recruiterJobs = jobRepository.findByRecruiterId(recruiterId);
        if (recruiterJobs.isEmpty()) {
            return List.of();
        }

        List<String> jobIds = recruiterJobs.stream().map(Job::getId).toList();
        List<Application> shortlisted = applicationRepository.findByJobIdInAndStatusOrderByAppliedAtDesc(jobIds, ApplicationStatus.SHORTLISTED);

        return shortlisted.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public boolean hasJobSeekerApplied(String jobSeekerId, String jobId) {
        if (jobSeekerId == null || jobId == null) return false;
        return applicationRepository.findByJobIdAndJobSeekerId(jobId, jobSeekerId)
                .map(app -> app.getId() != null && !app.getId().startsWith("discovery_"))
                .orElse(false);
    }

    private String resolveCompanyName(Job job) {
        if (job == null || job.getCompanyId() == null) return "TechCorp";
        return companyRepository.findById(job.getCompanyId())
                .map(Company::getName)
                .orElse("TechCorp");
    }

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (currentStatus == newStatus) return;
        if (currentStatus == ApplicationStatus.SELECTED || currentStatus == ApplicationStatus.REJECTED) {
            throw new BadRequestException("Cannot modify application status from terminal state: " + currentStatus);
        }
        if (newStatus == ApplicationStatus.APPLIED && currentStatus != ApplicationStatus.APPLIED) {
            throw new BadRequestException("Cannot revert application status back to APPLIED from " + currentStatus);
        }
    }

    private ApplicationResponse mapToResponse(Application application) {
        JobResponse jobInfo = null;
        try {
            Job job = jobRepository.findById(application.getJobId()).orElse(null);
            if (job != null) {
                jobInfo = jobService.mapToResponse(job);
            }
        } catch (Exception ignored) {}

        UserResponse applicantInfo = null;
        try {
            User user = userRepository.findById(application.getJobSeekerId()).orElse(null);
            if (user != null) {
                applicantInfo = UserResponse.fromUser(user);
            }
        } catch (Exception ignored) {}

        ResumeResponse resumeInfo = null;
        try {
            if (application.getResumeId() != null) {
                Resume resume = resumeRepository.findById(application.getResumeId()).orElse(null);
                if (resume != null) {
                    resumeInfo = ResumeResponse.fromEntity(resume);
                }
            }
        } catch (Exception ignored) {}

        return ApplicationResponse.fromEntity(application, jobInfo, applicantInfo, resumeInfo);
    }
}
