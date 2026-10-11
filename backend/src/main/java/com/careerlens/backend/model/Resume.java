package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "resumes")
public class Resume {

    @Id
    private String id;

    @Indexed
    private String jobSeekerId;

    private String originalFileName;
    private String storedFileName;
    private String fileType;
    private long fileSize;
    private String storagePath;

    private String extractedText;

    private ParsingStatus parsingStatus = ParsingStatus.PENDING;

    private List<String> skills = new ArrayList<>();
    private List<String> education = new ArrayList<>();
    private List<String> experience = new ArrayList<>();
    private List<String> projects = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();

    private String summary;

    private String title;
    private boolean isDefault = false;

    private Instant uploadedAt;
    private Instant updatedAt;

    public Resume() {}

    public Resume(String id, String jobSeekerId, String originalFileName, String storedFileName,
                  String fileType, long fileSize, String storagePath, String extractedText,
                  ParsingStatus parsingStatus, List<String> skills, List<String> education,
                  List<String> experience, List<String> projects, List<String> certifications,
                  String summary, Instant uploadedAt, Instant updatedAt) {
        this.id = id;
        this.jobSeekerId = jobSeekerId;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.extractedText = extractedText;
        this.parsingStatus = parsingStatus;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.education = education != null ? education : new ArrayList<>();
        this.experience = experience != null ? experience : new ArrayList<>();
        this.projects = projects != null ? projects : new ArrayList<>();
        this.certifications = certifications != null ? certifications : new ArrayList<>();
        this.summary = summary;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public ParsingStatus getParsingStatus() { return parsingStatus; }
    public void setParsingStatus(ParsingStatus parsingStatus) { this.parsingStatus = parsingStatus; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
