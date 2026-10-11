package com.careerlens.backend.dto;

import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class JobRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private List<String> requiredSkills;
    private List<String> preferredSkills;

    private Integer minimumExperience;
    private Integer maximumExperience;
    private String education;

    private Double salaryMin;
    private Double salaryMax;

    @NotBlank(message = "Job location is required")
    private String location;

    @NotNull(message = "Work mode is required")
    private WorkMode workMode;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private List<String> responsibilities;
    private List<String> requirements;

    private Boolean active = true;

    public JobRequest() {}

    public JobRequest(String title, String description, List<String> requiredSkills, List<String> preferredSkills,
                      Integer minimumExperience, Integer maximumExperience, String education, Double salaryMin,
                      Double salaryMax, String location, WorkMode workMode, JobType jobType,
                      List<String> responsibilities, List<String> requirements, Boolean active) {
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
        this.active = active != null ? active : true;
    }

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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
