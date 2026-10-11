package com.careerlens.backend.controller;

import com.careerlens.backend.dto.JobSeekerProfileResponse;
import com.careerlens.backend.dto.RecruiterProfileResponse;
import com.careerlens.backend.model.JobSeekerProfile;
import com.careerlens.backend.model.RecruiterProfile;
import com.careerlens.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/job-seeker/profile")
    public ResponseEntity<JobSeekerProfileResponse> getJobSeekerProfile(Authentication authentication) {
        JobSeekerProfileResponse response = profileService.getJobSeekerProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/job-seeker/profile/completeness")
    public ResponseEntity<com.careerlens.backend.dto.ProfileCompletenessResponse> getJobSeekerProfileCompleteness(Authentication authentication) {
        com.careerlens.backend.dto.ProfileCompletenessResponse response = profileService.getJobSeekerProfileCompleteness(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/job-seeker/profile")
    public ResponseEntity<JobSeekerProfileResponse> updateJobSeekerProfile(Authentication authentication,
                                                                          @Valid @RequestBody JobSeekerProfile updates) {
        JobSeekerProfileResponse response = profileService.updateJobSeekerProfile(authentication.getName(), updates);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recruiter/profile")
    public ResponseEntity<RecruiterProfileResponse> getRecruiterProfile(Authentication authentication) {
        RecruiterProfileResponse response = profileService.getRecruiterProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/recruiter/profile")
    public ResponseEntity<RecruiterProfileResponse> updateRecruiterProfile(Authentication authentication,
                                                                          @Valid @RequestBody RecruiterProfile updates) {
        RecruiterProfileResponse response = profileService.updateRecruiterProfile(authentication.getName(), updates);
        return ResponseEntity.ok(response);
    }
}
