package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "resume_analyses")
public class ResumeAnalysis {

    @Id
    private String id;

    @Indexed(unique = true)
    private String resumeId;

    @Indexed
    private String jobSeekerId;

    private int overallScore;
    private int skillsScore;
    private int experienceScore;
    private int educationScore;
    private int keywordScore;
    private int structureScore;

    private List<String> extractedSkills = new ArrayList<>();
    private List<String> extractedEducation = new ArrayList<>();
    private List<String> extractedExperience = new ArrayList<>();
    private List<String> projects = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();

    private List<Double> embedding = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();

    private String analysisMethod;
    private Instant analyzedAt = Instant.now();

    public ResumeAnalysis() {}

    public ResumeAnalysis(String id, String resumeId, String jobSeekerId, int overallScore, int skillsScore,
                          int experienceScore, int educationScore, int keywordScore, int structureScore,
                          List<String> extractedSkills, List<String> extractedEducation,
                          List<String> extractedExperience, List<String> projects, List<String> certifications,
                          List<Double> embedding, List<String> suggestions, String analysisMethod, Instant analyzedAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.jobSeekerId = jobSeekerId;
        this.overallScore = overallScore;
        this.skillsScore = skillsScore;
        this.experienceScore = experienceScore;
        this.educationScore = educationScore;
        this.keywordScore = keywordScore;
        this.structureScore = structureScore;
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
        this.extractedEducation = extractedEducation != null ? extractedEducation : new ArrayList<>();
        this.extractedExperience = extractedExperience != null ? extractedExperience : new ArrayList<>();
        this.projects = projects != null ? projects : new ArrayList<>();
        this.certifications = certifications != null ? certifications : new ArrayList<>();
        this.embedding = embedding != null ? embedding : new ArrayList<>();
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.analysisMethod = analysisMethod;
        this.analyzedAt = analyzedAt != null ? analyzedAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public int getSkillsScore() { return skillsScore; }
    public void setSkillsScore(int skillsScore) { this.skillsScore = skillsScore; }

    public int getExperienceScore() { return experienceScore; }
    public void setExperienceScore(int experienceScore) { this.experienceScore = experienceScore; }

    public int getEducationScore() { return educationScore; }
    public void setEducationScore(int educationScore) { this.educationScore = educationScore; }

    public int getKeywordScore() { return keywordScore; }
    public void setKeywordScore(int keywordScore) { this.keywordScore = keywordScore; }

    public int getStructureScore() { return structureScore; }
    public void setStructureScore(int structureScore) { this.structureScore = structureScore; }

    public List<String> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<String> extractedSkills) { this.extractedSkills = extractedSkills; }

    public List<String> getExtractedEducation() { return extractedEducation; }
    public void setExtractedEducation(List<String> extractedEducation) { this.extractedEducation = extractedEducation; }

    public List<String> getExtractedExperience() { return extractedExperience; }
    public void setExtractedExperience(List<String> extractedExperience) { this.extractedExperience = extractedExperience; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getAnalysisMethod() { return analysisMethod; }
    public void setAnalysisMethod(String analysisMethod) { this.analysisMethod = analysisMethod; }

    public Instant getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Instant analyzedAt) { this.analyzedAt = analyzedAt; }
}
