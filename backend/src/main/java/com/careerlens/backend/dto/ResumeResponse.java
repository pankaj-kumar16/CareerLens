package com.careerlens.backend.dto;

import com.careerlens.backend.model.ParsingStatus;
import com.careerlens.backend.model.Resume;

import java.time.Instant;
import java.util.List;

public class ResumeResponse {
    private String id;
    private String jobSeekerId;
    private String originalFileName;
    private String fileType;
    private long fileSize;
    private String extractedText;
    private ParsingStatus parsingStatus;
    private List<String> skills;
    private List<String> education;
    private List<String> experience;
    private List<String> projects;
    private List<String> certifications;
    private String summary;
    private String title;
    private boolean isDefault;
    private Instant uploadedAt;
    private Instant updatedAt;

    private String targetRole;

    public ResumeResponse() {}

    public ResumeResponse(String id, String jobSeekerId, String originalFileName, String fileType,
                          long fileSize, String extractedText, ParsingStatus parsingStatus,
                          List<String> skills, List<String> education, List<String> experience,
                          List<String> projects, List<String> certifications, String summary,
                          String title, boolean isDefault, Instant uploadedAt, Instant updatedAt) {
        this.id = id;
        this.jobSeekerId = jobSeekerId;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.extractedText = extractedText;
        this.parsingStatus = parsingStatus;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.projects = projects;
        this.certifications = certifications;
        this.summary = summary;
        this.title = title;
        this.isDefault = isDefault;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
        this.targetRole = com.careerlens.backend.util.RoleDetectorUtils.detectRole(title, originalFileName, skills, summary, extractedText);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJobSeekerId() { return jobSeekerId; }
    public void setJobSeekerId(String jobSeekerId) { this.jobSeekerId = jobSeekerId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

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

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public static ResumeResponse fromEntity(Resume resume) {
        if (resume == null) return null;
        ResumeResponse res = new ResumeResponse(
                resume.getId(),
                resume.getJobSeekerId(),
                resume.getOriginalFileName(),
                resume.getFileType(),
                resume.getFileSize(),
                resume.getExtractedText(),
                resume.getParsingStatus(),
                resume.getSkills(),
                resume.getEducation(),
                resume.getExperience(),
                resume.getProjects(),
                resume.getCertifications(),
                resume.getSummary(),
                resume.getTitle(),
                resume.isDefault(),
                resume.getUploadedAt(),
                resume.getUpdatedAt()
        );
        res.setTargetRole(com.careerlens.backend.util.RoleDetectorUtils.detectRole(resume));
        return res;
    }
}
