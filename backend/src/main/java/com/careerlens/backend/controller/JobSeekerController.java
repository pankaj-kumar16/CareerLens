package com.careerlens.backend.controller;

import com.careerlens.backend.dto.ApiResponse;
import com.careerlens.backend.dto.ApplicationResponse;
import com.careerlens.backend.dto.SavedJobResponse;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.service.ApplicationService;
import com.careerlens.backend.service.SavedJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-seeker")
public class JobSeekerController {

    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;
    private final UserRepository userRepository;

    public JobSeekerController(ApplicationService applicationService,
                                SavedJobService savedJobService,
                                UserRepository userRepository) {
        this.applicationService = applicationService;
        this.savedJobService = savedJobService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<ApplicationResponse> response = applicationService.getApplicationsForJobSeeker(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(Authentication authentication,
                                                                   @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        ApplicationResponse response = applicationService.getApplicationByIdForJobSeeker(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{jobId}/save")
    public ResponseEntity<SavedJobResponse> saveJob(Authentication authentication,
                                                    @PathVariable String jobId) {
        User user = getAuthenticatedUser(authentication);
        SavedJobResponse response = savedJobService.saveJob(user.getId(), jobId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/jobs/{jobId}/save")
    public ResponseEntity<ApiResponse<String>> unsaveJob(Authentication authentication,
                                                         @PathVariable String jobId) {
        User user = getAuthenticatedUser(authentication);
        savedJobService.unsaveJob(user.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.ok("Job removed from saved jobs."));
    }

    @GetMapping("/saved-jobs")
    public ResponseEntity<List<SavedJobResponse>> getSavedJobs(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<SavedJobResponse> response = savedJobService.getSavedJobs(user.getId());
        return ResponseEntity.ok(response);
    }
}
