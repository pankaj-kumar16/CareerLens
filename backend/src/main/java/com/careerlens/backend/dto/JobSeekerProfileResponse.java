package com.careerlens.backend.dto;

public class JobSeekerProfileResponse {

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

    public JobSeekerProfileResponse() {}

    public static JobSeekerProfileResponse from(com.careerlens.backend.model.JobSeekerProfile profile) {
        JobSeekerProfileResponse response = new JobSeekerProfileResponse();
        response.setUserId(profile.getUserId());
        response.setFullName(profile.getFullName());
        response.setPhone(profile.getPhone());
        response.setLocation(profile.getLocation());
        response.setSkills(profile.getSkills());
        response.setEducation(profile.getEducation());
        response.setExperience(profile.getExperience());
        response.setProjects(profile.getProjects());
        response.setCertifications(profile.getCertifications());
        response.setPreferredRoles(profile.getPreferredRoles());
        response.setPreferredLocations(profile.getPreferredLocations());
        response.setExpectedSalary(profile.getExpectedSalary());
        response.setWorkMode(profile.getWorkMode());
        response.setProfileCompleted(profile.isProfileCompleted());
        return response;
    }

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
}
