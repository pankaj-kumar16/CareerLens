package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "job_seeker_profiles")
public class JobSeekerProfile {

    @Id
    private String id;

    private String userId;
    private String fullName;
    private String phone;
    private String location;
    private String skills;
    private String education;
    private String experience;
    private String projects;
    private String certifications;
    private String preferredRoles;
    private String preferredLocations;
    private String expectedSalary;
    private String workMode;
    private boolean profileCompleted;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public JobSeekerProfile() {}

    public JobSeekerProfile(String id, String userId, String fullName, String phone, String location, String skills,
                            String education, String experience, String projects, String certifications,
                            String preferredRoles, String preferredLocations, String expectedSalary,
                            String workMode, boolean profileCompleted, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.location = location;
        this.skills = skills;
        this.education = education;
        this.experience = experience;
        this.projects = projects;
        this.certifications = certifications;
        this.preferredRoles = preferredRoles;
        this.preferredLocations = preferredLocations;
        this.expectedSalary = expectedSalary;
        this.workMode = workMode;
        this.profileCompleted = profileCompleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getPreferredRoles() { return preferredRoles; }
    public void setPreferredRoles(String preferredRoles) { this.preferredRoles = preferredRoles; }

    public String getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(String preferredLocations) { this.preferredLocations = preferredLocations; }

    public String getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(String expectedSalary) { this.expectedSalary = expectedSalary; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
