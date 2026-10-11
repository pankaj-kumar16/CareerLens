package com.careerlens.backend.dto;

import com.careerlens.backend.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ApplicationStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ApplicationStatus status;
    private Instant interviewDate;

    public ApplicationStatusUpdateRequest() {}

    public ApplicationStatusUpdateRequest(ApplicationStatus status, Instant interviewDate) {
        this.status = status;
        this.interviewDate = interviewDate;
    }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public Instant getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Instant interviewDate) { this.interviewDate = interviewDate; }
}
