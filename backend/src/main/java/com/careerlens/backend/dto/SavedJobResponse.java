package com.careerlens.backend.dto;

import com.careerlens.backend.model.SavedJob;

import java.time.Instant;

public class SavedJobResponse {

    private String id;
    private String jobSeekerId;
    private String jobId;
    private Instant savedAt;
    private JobResponse job;

    public SavedJobResponse() {}

    public SavedJobResponse(String id, String jobSeekerId, String jobId, Instant savedAt, JobResponse job) {
        this.id = id;
        this.jobSeekerId = jobSeekerId;
        this.jobId = jobId;
        this.savedAt = savedAt;
        this.job = job;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public Instant getSavedAt() { return savedAt; }
    public void setSavedAt(Instant savedAt) { this.savedAt = savedAt; }

    public JobResponse getJob() { return job; }
    public void setJob(JobResponse job) { this.job = job; }

    public static SavedJobResponse fromEntity(SavedJob savedJob, JobResponse jobResponse) {
        if (savedJob == null) return null;
        return new SavedJobResponse(
                savedJob.getId(),
                savedJob.getJobSeekerId(),
                savedJob.getJobId(),
                savedJob.getSavedAt(),
                jobResponse
        );
    }
}
