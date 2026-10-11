package com.careerlens.backend.dto;

import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;

import java.time.Instant;
import java.util.List;

public class RecommendationResponse {

    private String jobId;
    private String title;
    private String companyName;
    private String location;
    private WorkMode workMode;
    private String salaryRange;
    private JobType jobType;
    private double matchScore;
    private List<String> matchedSkills;
    private List<String> missingRequiredSkills;
    private List<String> missingPreferredSkills;
    private String explanation;
    private String analysisMethod;
    private boolean applied;
    private boolean saved;
    private Instant createdAt;

    public RecommendationResponse() {
    }

    public RecommendationResponse(String jobId, String title, String companyName, String location,
                                  WorkMode workMode, String salaryRange, JobType jobType, double matchScore,
                                  List<String> matchedSkills, List<String> missingRequiredSkills,
                                  List<String> missingPreferredSkills, String explanation,
                                  String analysisMethod, boolean applied, boolean saved, Instant createdAt) {
        this.jobId = jobId;
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.workMode = workMode;
        this.salaryRange = salaryRange;
        this.jobType = jobType;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills;
        this.missingRequiredSkills = missingRequiredSkills;
        this.missingPreferredSkills = missingPreferredSkills;
        this.explanation = explanation;
        this.analysisMethod = analysisMethod;
        this.applied = applied;
        this.saved = saved;
        this.createdAt = createdAt;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingRequiredSkills() {
        return missingRequiredSkills;
    }

    public void setMissingRequiredSkills(List<String> missingRequiredSkills) {
        this.missingRequiredSkills = missingRequiredSkills;
    }

    public List<String> getMissingPreferredSkills() {
        return missingPreferredSkills;
    }

    public void setMissingPreferredSkills(List<String> missingPreferredSkills) {
        this.missingPreferredSkills = missingPreferredSkills;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getAnalysisMethod() {
        return analysisMethod;
    }

    public void setAnalysisMethod(String analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
