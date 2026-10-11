package com.careerlens.backend.dto;

import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;

import java.time.Instant;
import java.util.List;

public class JobResponse {

    private String id;
    private String recruiterId;
    private String companyId;
    private String companyName;
    private String companyLogo;

    private String title;
    private String description;

    private List<String> requiredSkills;
    private List<String> preferredSkills;

    private Integer minimumExperience;
    private Integer maximumExperience;
    private String education;

    private Double salaryMin;
    private Double salaryMax;

    private String location;
    private WorkMode workMode;
    private JobType jobType;

    private List<String> responsibilities;
    private List<String> requirements;

    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    private Boolean hasApplied;
    private Boolean isSaved;

    public JobResponse() {}

    public JobResponse(String id, String recruiterId, String companyId, String companyName, String companyLogo,
                       String title, String description, List<String> requiredSkills, List<String> preferredSkills,
                       Integer minimumExperience, Integer maximumExperience, String education, Double salaryMin,
                       Double salaryMax, String location, WorkMode workMode, JobType jobType,
                       List<String> responsibilities, List<String> requirements, boolean active,
                       Instant createdAt, Instant updatedAt, Boolean hasApplied, Boolean isSaved) {
        this.id = id;
        this.recruiterId = recruiterId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyLogo = companyLogo;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.preferredSkills = preferredSkills;
        this.minimumExperience = minimumExperience;
        this.maximumExperience = maximumExperience;
        this.education = education;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.location = location;
        this.workMode = workMode;
        this.jobType = jobType;
        this.responsibilities = responsibilities;
        this.requirements = requirements;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hasApplied = hasApplied;
        this.isSaved = isSaved;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<String> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<String> preferredSkills) { this.preferredSkills = preferredSkills; }

    public Integer getMinimumExperience() { return minimumExperience; }
    public void setMinimumExperience(Integer minimumExperience) { this.minimumExperience = minimumExperience; }

    public Integer getMaximumExperience() { return maximumExperience; }
    public void setMaximumExperience(Integer maximumExperience) { this.maximumExperience = maximumExperience; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public Double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Double salaryMin) { this.salaryMin = salaryMin; }

    public Double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Double salaryMax) { this.salaryMax = salaryMax; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }

    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }

    public List<String> getResponsibilities() { return responsibilities; }
    public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getHasApplied() { return hasApplied; }
    public void setHasApplied(Boolean hasApplied) { this.hasApplied = hasApplied; }

    public Boolean getIsSaved() { return isSaved; }
    public void setIsSaved(Boolean isSaved) { this.isSaved = isSaved; }

    public static JobResponse fromEntity(Job job, String companyName, String companyLogo) {
        if (job == null) return null;
        return new JobResponse(
                job.getId(),
                job.getRecruiterId(),
                job.getCompanyId(),
                companyName != null ? companyName : "Company",
                companyLogo,
                job.getTitle(),
                job.getDescription(),
                job.getRequiredSkills(),
                job.getPreferredSkills(),
                job.getMinimumExperience(),
                job.getMaximumExperience(),
                job.getEducation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getLocation(),
                job.getWorkMode(),
                job.getJobType(),
                job.getResponsibilities(),
                job.getRequirements(),
                job.isActive(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                null,
                null
        );
    }
}
