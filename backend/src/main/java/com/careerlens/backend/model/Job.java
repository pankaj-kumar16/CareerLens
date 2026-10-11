package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    @Indexed
    private String recruiterId;

    @Indexed
    private String companyId;

    private String title;
    private String description;

    private List<String> requiredSkills = new ArrayList<>();
    private List<String> preferredSkills = new ArrayList<>();

    private Integer minimumExperience;
    private Integer maximumExperience;
    private String education;

    private Double salaryMin;
    private Double salaryMax;

    private String location;
    private WorkMode workMode;
    private JobType jobType;

    private List<String> responsibilities = new ArrayList<>();
    private List<String> requirements = new ArrayList<>();

    @Indexed
    private boolean active = true;

    @Indexed
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Job() {}

    public Job(String id, String recruiterId, String companyId, String title, String description,
               List<String> requiredSkills, List<String> preferredSkills, Integer minimumExperience,
               Integer maximumExperience, String education, Double salaryMin, Double salaryMax,
               String location, WorkMode workMode, JobType jobType, List<String> responsibilities,
               List<String> requirements, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.recruiterId = recruiterId;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.preferredSkills = preferredSkills != null ? preferredSkills : new ArrayList<>();
        this.minimumExperience = minimumExperience;
        this.maximumExperience = maximumExperience;
        this.education = education;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.location = location;
        this.workMode = workMode;
        this.jobType = jobType;
        this.responsibilities = responsibilities != null ? responsibilities : new ArrayList<>();
        this.requirements = requirements != null ? requirements : new ArrayList<>();
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRecruiterId() { return recruiterId; }
    public void setRecruiterId(String recruiterId) { this.recruiterId = recruiterId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

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
}
