package com.careerlens.backend.dto;

public class JobPerformanceData {
    private String jobId;
    private String title;
    private String companyName;
    private boolean active;
    private long totalApplicants;
    private long shortlisted;
    private long interviews;
    private long selected;
    private long rejected;
    private double averageMatchScore;

    public JobPerformanceData() {
    }

    public JobPerformanceData(String jobId, String title, String companyName, boolean active,
                              long totalApplicants, long shortlisted, long interviews,
                              long selected, long rejected, double averageMatchScore) {
        this.jobId = jobId;
        this.title = title;
        this.companyName = companyName;
        this.active = active;
        this.totalApplicants = totalApplicants;
        this.shortlisted = shortlisted;
        this.interviews = interviews;
        this.selected = selected;
        this.rejected = rejected;
        this.averageMatchScore = averageMatchScore;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public double getAverageMatchScore() {
        return averageMatchScore;
    }

    public void setAverageMatchScore(double averageMatchScore) {
        this.averageMatchScore = averageMatchScore;
    }
}
