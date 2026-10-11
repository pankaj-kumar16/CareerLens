package com.careerlens.backend.dto;

import com.careerlens.backend.model.Application;
import com.careerlens.backend.model.ApplicationStatus;

import java.time.Instant;
import java.util.List;

public class ApplicationResponse {

    private String id;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String companyLogo;
    private String location;

    private String jobSeekerId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;

    private String resumeId;
    private String resumeOriginalFileName;

    private ApplicationStatus status;
    private Instant appliedAt;
    private Instant updatedAt;
    private String recruiterNotes;
    private Instant interviewDate;

    private List<String> applicantSkills;
    private List<String> applicantEducation;
    private List<String> applicantExperience;

    public ApplicationResponse() {}

    public ApplicationResponse(String id, String jobId, String jobTitle, String companyName, String companyLogo,
                               String location, String jobSeekerId, String applicantName, String applicantEmail,
                               String applicantPhone, String resumeId, String resumeOriginalFileName,
                               ApplicationStatus status, Instant appliedAt, Instant updatedAt, String recruiterNotes,
                               Instant interviewDate, List<String> applicantSkills, List<String> applicantEducation,
                               List<String> applicantExperience) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.location = location;
        this.jobSeekerId = jobSeekerId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.applicantPhone = applicantPhone;
        this.resumeId = resumeId;
        this.resumeOriginalFileName = resumeOriginalFileName;
        this.status = status;
        this.appliedAt = appliedAt;
        this.updatedAt = updatedAt;
        this.recruiterNotes = recruiterNotes;
        this.interviewDate = interviewDate;
        this.applicantSkills = applicantSkills;
        this.applicantEducation = applicantEducation;
        this.applicantExperience = applicantExperience;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getResumeOriginalFileName() { return resumeOriginalFileName; }
    public void setResumeOriginalFileName(String resumeOriginalFileName) { this.resumeOriginalFileName = resumeOriginalFileName; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }

    public Instant getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Instant interviewDate) { this.interviewDate = interviewDate; }

    public List<String> getApplicantSkills() { return applicantSkills; }
    public void setApplicantSkills(List<String> applicantSkills) { this.applicantSkills = applicantSkills; }

    public List<String> getApplicantEducation() { return applicantEducation; }
    public void setApplicantEducation(List<String> applicantEducation) { this.applicantEducation = applicantEducation; }

    public List<String> getApplicantExperience() { return applicantExperience; }
    public void setApplicantExperience(List<String> applicantExperience) { this.applicantExperience = applicantExperience; }

    public static ApplicationResponse fromEntity(Application application,
                                                  JobResponse jobInfo,
                                                  UserResponse applicantInfo,
                                                  ResumeResponse resumeInfo) {
        if (application == null) return null;

        return new ApplicationResponse(
                application.getId(),
                application.getJobId(),
                jobInfo != null ? jobInfo.getTitle() : null,
                jobInfo != null ? jobInfo.getCompanyName() : null,
                jobInfo != null ? jobInfo.getCompanyLogo() : null,
                jobInfo != null ? jobInfo.getLocation() : null,
                application.getJobSeekerId(),
                applicantInfo != null ? applicantInfo.getFullName() : null,
                applicantInfo != null ? applicantInfo.getEmail() : null,
                applicantInfo != null ? applicantInfo.getPhone() : null,
                application.getResumeId(),
                resumeInfo != null ? resumeInfo.getOriginalFileName() : null,
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt(),
                application.getRecruiterNotes(),
                application.getInterviewDate(),
                resumeInfo != null ? resumeInfo.getSkills() : null,
                resumeInfo != null ? resumeInfo.getEducation() : null,
                resumeInfo != null ? resumeInfo.getExperience() : null
        );
    }
}
