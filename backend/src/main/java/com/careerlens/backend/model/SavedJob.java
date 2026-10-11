package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "saved_jobs")
@CompoundIndex(name = "jobSeeker_job_idx", def = "{'jobSeekerId': 1, 'jobId': 1}", unique = true)
public class SavedJob {

    @Id
    private String id;

    @Indexed
    private String jobSeekerId;

    private String jobId;

    private Instant savedAt = Instant.now();

    public SavedJob() {}

    public SavedJob(String id, String jobSeekerId, String jobId, Instant savedAt) {
        this.id = id;
        this.jobSeekerId = jobSeekerId;
        this.jobId = jobId;
        this.savedAt = savedAt != null ? savedAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public Instant getSavedAt() { return savedAt; }
    public void setSavedAt(Instant savedAt) { this.savedAt = savedAt; }
}
