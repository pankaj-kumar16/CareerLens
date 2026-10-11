package com.careerlens.backend.controller;

import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.dto.RecommendationResponse;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.MatchResult;
import com.careerlens.backend.model.ResumeAnalysis;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.service.JobService;
import com.careerlens.backend.service.RecommendationService;
import com.careerlens.backend.service.ResumeAnalysisService;
import com.careerlens.backend.service.ResumeMatchingService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/job-seeker")
public class AiAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;
    private final ResumeMatchingService resumeMatchingService;
    private final RecommendationService recommendationService;
    private final JobRepository jobRepository;
    private final JobService jobService;
    private final UserRepository userRepository;

    public AiAnalysisController(ResumeAnalysisService resumeAnalysisService,
                                ResumeMatchingService resumeMatchingService,
                                RecommendationService recommendationService,
                                JobRepository jobRepository,
                                JobService jobService,
                                UserRepository userRepository) {
        this.resumeAnalysisService = resumeAnalysisService;
        this.resumeMatchingService = resumeMatchingService;
        this.recommendationService = recommendationService;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    @GetMapping("/resume/analysis")
    public ResponseEntity<ResumeAnalysis> getResumeAnalysis(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(user.getId());
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/jobs/{jobId}/match")
    public ResponseEntity<MatchResult> getJobMatch(Authentication authentication,
                                                   @PathVariable String jobId) {
        User user = getAuthenticatedUser(authentication);
        MatchResult result = resumeMatchingService.matchJobSeekerToJob(user.getId(), jobId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Page<RecommendationResponse>> getRecommendations(Authentication authentication,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size) {
        User user = getAuthenticatedUser(authentication);
        Page<RecommendationResponse> recommendations = recommendationService.getRecommendationsForJobSeeker(user.getId(), page, size);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Map<String, Object>>> getRecommendedMatches(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Page<RecommendationResponse> page = recommendationService.getRecommendationsForJobSeeker(user.getId(), 0, 50);

        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (RecommendationResponse recResp : page.getContent()) {
            Job job = jobRepository.findById(recResp.getJobId()).orElse(null);
            if (job == null) continue;
            JobResponse jobResp = jobService.mapToResponse(job);

            MatchResult match = new MatchResult();
            match.setJobId(recResp.getJobId());
            match.setOverallScore((int) Math.round(recResp.getMatchScore()));
            match.setMatchedSkills(recResp.getMatchedSkills());
            match.setMissingRequiredSkills(recResp.getMissingRequiredSkills());
            match.setMissingPreferredSkills(recResp.getMissingPreferredSkills());
            match.setExplanation(recResp.getExplanation());
            match.setAnalysisMethod(recResp.getAnalysisMethod());

            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("job", jobResp);
            rec.put("match", match);
            recommendations.add(rec);
        }

        return ResponseEntity.ok(recommendations);
    }
}
