package com.careerlens.backend.dto;

import com.careerlens.backend.model.ApplicationStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CandidateRankingResponse {
    private String applicationId;
    private String jobSeekerId;
    private String resumeId;
    private String candidateName;
    private String email;
    private String phone;
    private ApplicationStatus applicationStatus;
    private int matchScore;
    private double overallMatchScore;
    private double skillScore;
    private double semanticScore;
    private double experienceScore;
    private double educationScore;
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingRequiredSkills = new ArrayList<>();
    private List<String> applicantSkills = new ArrayList<>();
    private List<String> applicantEducation = new ArrayList<>();
    private String explanation;
    private Instant appliedAt;
    private String resumeOriginalFileName;

    public CandidateRankingResponse() {}

    public CandidateRankingResponse(String applicationId, String jobSeekerId, String resumeId, String candidateName,
                                   String email, String phone, ApplicationStatus applicationStatus, int matchScore,
                                   List<String> matchedSkills, List<String> missingRequiredSkills, String explanation,
                                   Instant appliedAt) {
        this.applicationId = applicationId;
        this.jobSeekerId = jobSeekerId;
        this.resumeId = resumeId;
        this.candidateName = candidateName;
        this.email = email;
        this.phone = phone;
        this.applicationStatus = applicationStatus;
        this.matchScore = matchScore;
        this.overallMatchScore = matchScore;
        this.matchedSkills = matchedSkills;
        this.missingRequiredSkills = missingRequiredSkills;
        this.explanation = explanation;
        this.appliedAt = appliedAt;
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCandidateEmail() { return email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCandidatePhone() { return phone; }

    public ApplicationStatus getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(ApplicationStatus applicationStatus) { this.applicationStatus = applicationStatus; }

    public ApplicationStatus getStatus() { return applicationStatus; }
    public void setStatus(ApplicationStatus status) { this.applicationStatus = status; }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { 
        this.matchScore = matchScore; 
        this.overallMatchScore = matchScore;
    }

    public double getOverallMatchScore() { return overallMatchScore > 0 ? overallMatchScore : matchScore; }
    public void setOverallMatchScore(double overallMatchScore) { 
        this.overallMatchScore = overallMatchScore;
        this.matchScore = (int) Math.round(overallMatchScore);
    }

    public double getSkillScore() { return skillScore; }
    public void setSkillScore(double skillScore) { this.skillScore = skillScore; }

    public double getSemanticScore() { return semanticScore; }
    public void setSemanticScore(double semanticScore) { this.semanticScore = semanticScore; }

    public double getExperienceScore() { return experienceScore; }
    public void setExperienceScore(double experienceScore) { this.experienceScore = experienceScore; }

    public double getEducationScore() { return educationScore; }
    public void setEducationScore(double educationScore) { this.educationScore = educationScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(List<String> missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }

    public List<String> getApplicantSkills() { return applicantSkills; }
    public void setApplicantSkills(List<String> applicantSkills) { this.applicantSkills = applicantSkills; }

    public List<String> getApplicantEducation() { return applicantEducation; }
    public void setApplicantEducation(List<String> applicantEducation) { this.applicantEducation = applicantEducation; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }

    public String getResumeOriginalFileName() { return resumeOriginalFileName; }
    public void setResumeOriginalFileName(String resumeOriginalFileName) { this.resumeOriginalFileName = resumeOriginalFileName; }
}
