package com.careerlens.backend.dto;

public class TrendDataPoint {
    private String date;
    private long applications;

    public TrendDataPoint() {
    }

    public TrendDataPoint(String date, long applications) {
        this.date = date;
        this.applications = applications;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getApplications() {
        return applications;
    }

    public void setApplications(long applications) {
        this.applications = applications;
    }
}
