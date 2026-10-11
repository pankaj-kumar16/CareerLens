package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "match_results")
@CompoundIndex(name = "resume_job_idx", def = "{'resumeId': 1, 'jobId': 1}", unique = true)
public class MatchResult {

    @Id
    private String id;

    @Indexed
    private String resumeId;

    @Indexed
    private String jobSeekerId;

    @Indexed
    private String jobId;

    private int overallScore;
    private int skillScore;
    private int semanticScore;
    private int experienceScore;
    private int educationScore;
    private int requirementScore;

    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingRequiredSkills = new ArrayList<>();
    private List<String> missingPreferredSkills = new ArrayList<>();

    private String explanation;
    private String analysisMethod;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public MatchResult() {}

    public MatchResult(String id, String resumeId, String jobSeekerId, String jobId, int overallScore,
                       int skillScore, int semanticScore, int experienceScore, int educationScore,
                       int requirementScore, List<String> matchedSkills, List<String> missingRequiredSkills,
                       List<String> missingPreferredSkills, String explanation, String analysisMethod,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.jobSeekerId = jobSeekerId;
        this.jobId = jobId;
        this.overallScore = overallScore;
        this.skillScore = skillScore;
        this.semanticScore = semanticScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.requirementScore = requirementScore;
        this.matchedSkills = matchedSkills != null ? matchedSkills : new ArrayList<>();
        this.missingRequiredSkills = missingRequiredSkills != null ? missingRequiredSkills : new ArrayList<>();
        this.missingPreferredSkills = missingPreferredSkills != null ? missingPreferredSkills : new ArrayList<>();
        this.explanation = explanation;
        this.analysisMethod = analysisMethod;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public int getSkillScore() { return skillScore; }
    public void setSkillScore(int skillScore) { this.skillScore = skillScore; }

    public int getSemanticScore() { return semanticScore; }
    public void setSemanticScore(int semanticScore) { this.semanticScore = semanticScore; }

    public int getExperienceScore() { return experienceScore; }
    public void setExperienceScore(int experienceScore) { this.experienceScore = experienceScore; }

    public int getEducationScore() { return educationScore; }
    public void setEducationScore(int educationScore) { this.educationScore = educationScore; }

    public int getRequirementScore() { return requirementScore; }
    public void setRequirementScore(int requirementScore) { this.requirementScore = requirementScore; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingRequiredSkills() { return missingRequiredSkills; }
    public void setMissingRequiredSkills(List<String> missingRequiredSkills) { this.missingRequiredSkills = missingRequiredSkills; }

    public List<String> getMissingPreferredSkills() { return missingPreferredSkills; }
    public void setMissingPreferredSkills(List<String> missingPreferredSkills) { this.missingPreferredSkills = missingPreferredSkills; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getAnalysisMethod() { return analysisMethod; }
    public void setAnalysisMethod(String analysisMethod) { this.analysisMethod = analysisMethod; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
