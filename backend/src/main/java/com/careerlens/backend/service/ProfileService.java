package com.careerlens.backend.service;

import com.careerlens.backend.dto.CompanyResponse;
import com.careerlens.backend.dto.JobSeekerProfileResponse;
import com.careerlens.backend.dto.RecruiterProfileResponse;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.JobSeekerProfile;
import com.careerlens.backend.model.RecruiterProfile;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobSeekerProfileRepository;
import com.careerlens.backend.repository.RecruiterProfileRepository;
import com.careerlens.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

import com.careerlens.backend.dto.ProfileCompletenessResponse;
import com.careerlens.backend.repository.ResumeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final ResumeRepository resumeRepository;

    public ProfileService(UserRepository userRepository,
                          JobSeekerProfileRepository jobSeekerProfileRepository,
                          RecruiterProfileRepository recruiterProfileRepository,
                          ResumeRepository resumeRepository) {
        this.userRepository = userRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.resumeRepository = resumeRepository;
    }

    public JobSeekerProfileResponse getJobSeekerProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return jobSeekerProfileRepository.findByUserId(user.getId())
                .map(JobSeekerProfileResponse::from)
                .orElseGet(() -> JobSeekerProfileResponse.from(new JobSeekerProfile(null, user.getId(), user.getFullName(), user.getPhone(), null, null, null, null, null, null, null, null, null, null, false, Instant.now(), Instant.now())));
    }

    public ProfileCompletenessResponse getJobSeekerProfileCompleteness(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        JobSeekerProfile profile = jobSeekerProfileRepository.findByUserId(user.getId()).orElse(null);
        long resumeCount = resumeRepository.countByJobSeekerId(user.getId());

        List<ProfileCompletenessResponse.SectionStatus> sections = new ArrayList<>();
        List<String> missingFields = new ArrayList<>();
        int totalPercentage = 0;

        // 1. Basic Information (15%)
        boolean basicComplete = isNonEmpty(user.getFullName()) && (isNonEmpty(user.getPhone()) || (profile != null && isNonEmpty(profile.getPhone()))) && (profile != null && isNonEmpty(profile.getLocation()));
        int basicWeight = 15;
        if (basicComplete) {
            totalPercentage += basicWeight;
        } else {
            missingFields.add("Add location and phone number");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Basic Information", basicWeight, basicComplete, basicComplete ? "Completed" : "Name, phone, and location required"));

        // 2. Professional Info & Skills (20%)
        boolean skillsComplete = profile != null && (isNonEmpty(profile.getSkills()) || isNonEmpty(profile.getPreferredRoles()));
        int skillsWeight = 20;
        if (skillsComplete) {
            totalPercentage += skillsWeight;
        } else {
            missingFields.add("Add key skills & preferred roles");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Skills & Role Preferences", skillsWeight, skillsComplete, skillsComplete ? "Completed" : "Skills or job preferences required"));

        // 3. Education (15%)
        boolean eduComplete = profile != null && isNonEmpty(profile.getEducation());
        int eduWeight = 15;
        if (eduComplete) {
            totalPercentage += eduWeight;
        } else {
            missingFields.add("Add education details");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Education", eduWeight, eduComplete, eduComplete ? "Completed" : "Degree / university details required"));

        // 4. Experience (15%)
        boolean expComplete = profile != null && isNonEmpty(profile.getExperience());
        int expWeight = 15;
        if (expComplete) {
            totalPercentage += expWeight;
        } else {
            missingFields.add("Add work experience");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Work Experience", expWeight, expComplete, expComplete ? "Completed" : "Work history or internship details required"));

        // 5. Projects (10%)
        boolean projComplete = profile != null && isNonEmpty(profile.getProjects());
        int projWeight = 10;
        if (projComplete) {
            totalPercentage += projWeight;
        } else {
            missingFields.add("Add projects or portfolio");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Projects", projWeight, projComplete, projComplete ? "Completed" : "Key projects required"));

        // 6. Certifications (10%)
        boolean certComplete = profile != null && isNonEmpty(profile.getCertifications());
        int certWeight = 10;
        if (certComplete) {
            totalPercentage += certWeight;
        } else {
            missingFields.add("Add certifications");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Certifications", certWeight, certComplete, certComplete ? "Completed" : "Professional certifications required"));

        // 7. Resume Upload (15%)
        boolean resumeComplete = resumeCount > 0;
        int resumeWeight = 15;
        if (resumeComplete) {
            totalPercentage += resumeWeight;
        } else {
            missingFields.add("Upload a resume");
        }
        sections.add(new ProfileCompletenessResponse.SectionStatus("Resume", resumeWeight, resumeComplete, resumeComplete ? "Completed (" + resumeCount + " version" + (resumeCount > 1 ? "s" : "") + ")" : "At least 1 uploaded resume required"));

        String tip = missingFields.isEmpty()
                ? "Your profile is 100% complete! You are ready for top recruiters."
                : "Complete " + missingFields.get(0).toLowerCase() + " to improve your profile.";

        return new ProfileCompletenessResponse(totalPercentage, sections, missingFields, tip);
    }

    private boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public JobSeekerProfileResponse updateJobSeekerProfile(String userEmail, JobSeekerProfile updates) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        JobSeekerProfile profile = jobSeekerProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> new JobSeekerProfile(null, user.getId(), null, null, null, null, null, null, null, null, null, null, null, null, false, Instant.now(), Instant.now()));

        if (updates.getFullName() != null) profile.setFullName(updates.getFullName());
        if (updates.getPhone() != null) profile.setPhone(updates.getPhone());
        if (updates.getLocation() != null) profile.setLocation(updates.getLocation());
        if (updates.getSkills() != null) profile.setSkills(updates.getSkills());
        if (updates.getEducation() != null) profile.setEducation(updates.getEducation());
        if (updates.getExperience() != null) profile.setExperience(updates.getExperience());
        if (updates.getProjects() != null) profile.setProjects(updates.getProjects());
        if (updates.getCertifications() != null) profile.setCertifications(updates.getCertifications());
        if (updates.getPreferredRoles() != null) profile.setPreferredRoles(updates.getPreferredRoles());
        if (updates.getPreferredLocations() != null) profile.setPreferredLocations(updates.getPreferredLocations());
        if (updates.getExpectedSalary() != null) profile.setExpectedSalary(updates.getExpectedSalary());
        if (updates.getWorkMode() != null) profile.setWorkMode(updates.getWorkMode());

        profile.setProfileCompleted(true);
        profile.setUpdatedAt(Instant.now());

        jobSeekerProfileRepository.save(profile);
        return JobSeekerProfileResponse.from(profile);
    }

    public RecruiterProfileResponse getRecruiterProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return recruiterProfileRepository.findByUserId(user.getId())
                .map(RecruiterProfileResponse::from)
                .orElseGet(() -> RecruiterProfileResponse.from(new RecruiterProfile(null, user.getId(), user.getFullName(), user.getPhone(), null, null, Instant.now(), Instant.now())));
    }

    public RecruiterProfileResponse updateRecruiterProfile(String userEmail, RecruiterProfile updates) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        RecruiterProfile profile = recruiterProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> new RecruiterProfile(null, user.getId(), null, null, null, null, Instant.now(), Instant.now()));

        if (updates.getFullName() != null) profile.setFullName(updates.getFullName());
        if (updates.getPhone() != null) profile.setPhone(updates.getPhone());
        if (updates.getPosition() != null) profile.setPosition(updates.getPosition());
        if (updates.getCompanyId() != null) profile.setCompanyId(updates.getCompanyId());

        profile.setUpdatedAt(Instant.now());

        recruiterProfileRepository.save(profile);
        return RecruiterProfileResponse.from(profile);
    }
}
