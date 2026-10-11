package com.careerlens.backend.controller;

import com.careerlens.backend.dto.CandidateRankingResponse;
import com.careerlens.backend.dto.RecruiterAnalyticsResponse;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.service.AnalyticsService;
import com.careerlens.backend.service.ResumeMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterAiController {

    private final ResumeMatchingService resumeMatchingService;
    private final AnalyticsService analyticsService;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public RecruiterAiController(ResumeMatchingService resumeMatchingService,
                                 AnalyticsService analyticsService,
                                 JobRepository jobRepository,
                                 UserRepository userRepository) {
        this.resumeMatchingService = resumeMatchingService;
        this.analyticsService = analyticsService;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated recruiter not found."));
    }

    @GetMapping("/analytics")
    public ResponseEntity<RecruiterAnalyticsResponse> getAnalytics(
            Authentication authentication,
            @RequestParam(defaultValue = "30") int days
    ) {
        User user = getAuthenticatedUser(authentication);
        RecruiterAnalyticsResponse analytics = analyticsService.getRecruiterAnalytics(user.getId(), days);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/jobs/{jobId}/candidates")
    public ResponseEntity<List<CandidateRankingResponse>> getRankedCandidates(
            Authentication authentication,
            @PathVariable String jobId
    ) {
        User user = getAuthenticatedUser(authentication);
        List<CandidateRankingResponse> candidates = resumeMatchingService.rankCandidatesForJob(jobId, user.getId());
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<CandidateRankingResponse>> getAllTopMatches(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<Job> recruiterJobs = jobRepository.findByRecruiterId(user.getId());

        List<CandidateRankingResponse> allCandidates = new ArrayList<>();
        for (Job job : recruiterJobs) {
            try {
                List<CandidateRankingResponse> jobCandidates = resumeMatchingService.rankCandidatesForJob(job.getId(), user.getId());
                allCandidates.addAll(jobCandidates);
            } catch (Exception ignored) {}
        }

        allCandidates.sort((c1, c2) -> Integer.compare(c2.getMatchScore(), c1.getMatchScore()));
        return ResponseEntity.ok(allCandidates);
    }
}
