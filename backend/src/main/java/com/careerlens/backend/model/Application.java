package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "applications")
@CompoundIndex(name = "job_jobSeeker_idx", def = "{'jobId': 1, 'jobSeekerId': 1}", unique = true)
public class Application {

    @Id
    private String id;

    private String jobId;

    @Indexed
    private String jobSeekerId;

    private String resumeId;

    @Indexed
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private Instant appliedAt = Instant.now();
    private Instant updatedAt = Instant.now();

    private String recruiterNotes;
    private Instant interviewDate;

    public Application() {}

    public Application(String id, String jobId, String jobSeekerId, String resumeId, ApplicationStatus status,
                       Instant appliedAt, Instant updatedAt, String recruiterNotes, Instant interviewDate) {
        this.id = id;
        this.jobId = jobId;
        this.jobSeekerId = jobSeekerId;
        this.resumeId = resumeId;
        this.status = status != null ? status : ApplicationStatus.APPLIED;
        this.appliedAt = appliedAt != null ? appliedAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.recruiterNotes = recruiterNotes;
        this.interviewDate = interviewDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

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
}
