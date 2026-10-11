package com.careerlens.backend.dto;

import java.util.List;

public class RecruiterAnalyticsResponse {

    private long activeJobs;
    private long totalApplicants;
    private long shortlisted;
    private long interviews;
    private long selected;
    private long rejected;
    private List<TrendDataPoint> applicationTrend;
    private List<JobPerformanceData> jobPerformance;

    public RecruiterAnalyticsResponse() {
    }

    public RecruiterAnalyticsResponse(long activeJobs, long totalApplicants, long shortlisted,
                                      long interviews, long selected, long rejected,
                                      List<TrendDataPoint> applicationTrend,
                                      List<JobPerformanceData> jobPerformance) {
        this.activeJobs = activeJobs;
        this.totalApplicants = totalApplicants;
        this.shortlisted = shortlisted;
        this.interviews = interviews;
        this.selected = selected;
        this.rejected = rejected;
        this.applicationTrend = applicationTrend;
        this.jobPerformance = jobPerformance;
    }

    public long getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(long activeJobs) {
        this.activeJobs = activeJobs;
    }

    public long getTotalApplicants() {
        return totalApplicants;
    }

    public void setTotalApplicants(long totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    public long getShortlisted() {
        return shortlisted;
    }

    public void setShortlisted(long shortlisted) {
        this.shortlisted = shortlisted;
    }

    public long getInterviews() {
        return interviews;
    }

    public void setInterviews(long interviews) {
        this.interviews = interviews;
    }

    public long getSelected() {
        return selected;
    }

    public void setSelected(long selected) {
        this.selected = selected;
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = rejected;
    }

    public List<TrendDataPoint> getApplicationTrend() {
        return applicationTrend;
    }

    public void setApplicationTrend(List<TrendDataPoint> applicationTrend) {
        this.applicationTrend = applicationTrend;
    }

    public List<JobPerformanceData> getJobPerformance() {
        return jobPerformance;
    }

    public void setJobPerformance(List<JobPerformanceData> jobPerformance) {
        this.jobPerformance = jobPerformance;
    }
}
