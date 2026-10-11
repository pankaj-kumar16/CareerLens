package com.careerlens.backend.service;

import com.careerlens.backend.dto.RecommendationResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.model.Application;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.MatchResult;
import com.careerlens.backend.model.Resume;
import com.careerlens.backend.repository.ApplicationRepository;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.ResumeRepository;
import com.careerlens.backend.repository.SavedJobRepository;
import com.careerlens.backend.util.RoleCompatibilityUtils;
import com.careerlens.backend.util.RoleDetectorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class RecommendationService {

    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final CompanyRepository companyRepository;
    private final ResumeMatchingService resumeMatchingService;

    @Value("${careerlens.recommendation.min-score:0.0}")
    private double minScore;

    public RecommendationService(JobRepository jobRepository,
                                 ResumeRepository resumeRepository,
                                 ApplicationRepository applicationRepository,
                                 SavedJobRepository savedJobRepository,
                                 CompanyRepository companyRepository,
                                 ResumeMatchingService resumeMatchingService) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.companyRepository = companyRepository;
        this.resumeMatchingService = resumeMatchingService;
    }

    /**
     * Recommended job eligibility algorithm:
     * 1. Fetch candidate's actual uploaded resume versions.
     * 2. Obtain each resume's detected primary role.
     * 3. Normalize each role and job title.
     * 4. Apply explicit, maintainable role-compatibility mapping.
     * 5. Filter active published jobs using strict role compatibility.
     * 6. Combine eligible jobs across all resumes and deduplicate by job ID.
     * 7. Rank eligible jobs by skill match & match score.
     * 8. Return only eligible jobs to Recommended Jobs section.
     */
    public Page<RecommendationResponse> getRecommendationsForJobSeeker(String jobSeekerId, int page, int size) {
        // 1. Load ALL resumes for this job seeker (supports multiple resume versions)
        List<Resume> allResumes = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
        if (allResumes.isEmpty()) {
            throw new BadRequestException("Please upload a resume first to receive personalized AI job recommendations.");
        }

        // 2. Extract primary professional roles across ALL uploaded resume versions
        Map<String, Set<String>> rolesByResumeId = new LinkedHashMap<>();
        Set<String> allCandidateRoles = new LinkedHashSet<>();

        for (Resume resume : allResumes) {
            String detectedRole = RoleDetectorUtils.detectRole(resume);
            if (StringUtils.hasText(detectedRole)) {
                allCandidateRoles.add(detectedRole.toLowerCase());
                rolesByResumeId.computeIfAbsent(resume.getId(), k -> new LinkedHashSet<>()).add(detectedRole.toLowerCase());
            }
        }

        // Identify default resume for fallback
        Resume defaultResume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                .or(() -> allResumes.stream().findFirst())
                .orElse(allResumes.get(0));

        // Get all active jobs
        List<Job> allActiveJobs = jobRepository.findAll().stream()
                .filter(Job::isActive)
                .toList();

        // Clean up legacy virtual discovery application records stored by earlier bug
        try {
            List<Application> legacyDiscovery = applicationRepository.findAll().stream()
                    .filter(a -> a.getId() != null && a.getId().startsWith("discovery_"))
                    .toList();
            if (!legacyDiscovery.isEmpty()) {
                applicationRepository.deleteAll(legacyDiscovery);
            }
        } catch (Exception ignored) {}

        Map<String, RecommendationResponse> deduplicatedRecs = new LinkedHashMap<>();

        for (Job job : allActiveJobs) {
            // Skip already applied jobs (ignore virtual discovery records)
            boolean hasRealApplication = applicationRepository.findByJobIdAndJobSeekerId(job.getId(), jobSeekerId)
                    .map(app -> app.getId() != null && !app.getId().startsWith("discovery_"))
                    .orElse(false);
            if (hasRealApplication) {
                continue;
            }

            // 5. STRICT ROLE COMPATIBILITY FILTER: Check if job title matches candidate resume roles
            if (!allCandidateRoles.isEmpty() && !RoleCompatibilityUtils.isCompatible(job, allCandidateRoles)) {
                continue; // Exclude unrelated roles!
            }

            // Calculate match score using resumes compatible with this job
            MatchResult bestMatch = null;
            String matchedRoleName = null;

            for (Resume resume : allResumes) {
                Set<String> resumeRoles = rolesByResumeId.getOrDefault(resume.getId(), Set.of());
                if (resumeRoles.isEmpty() || RoleCompatibilityUtils.isCompatible(job, resumeRoles)) {
                    try {
                        MatchResult match = resumeMatchingService.calculateMatch(resume, job);
                        if (bestMatch == null || match.getOverallScore() > bestMatch.getOverallScore()) {
                            bestMatch = match;
                            matchedRoleName = resumeRoles.isEmpty() ? null : resumeRoles.iterator().next();
                        }
                    } catch (Exception ignored) {}
                }
            }

            // Fallback to default resume match if bestMatch is still null
            if (bestMatch == null) {
                try {
                    bestMatch = resumeMatchingService.calculateMatch(defaultResume, job);
                } catch (Exception ignored) {}
            }

            double matchScore = (bestMatch != null) ? bestMatch.getOverallScore() : 75.0;
            List<String> matchedSkills = (bestMatch != null && bestMatch.getMatchedSkills() != null) ? bestMatch.getMatchedSkills() : List.of();
            List<String> missingReq = (bestMatch != null && bestMatch.getMissingRequiredSkills() != null) ? bestMatch.getMissingRequiredSkills() : List.of();
            List<String> missingPref = (bestMatch != null && bestMatch.getMissingPreferredSkills() != null) ? bestMatch.getMissingPreferredSkills() : List.of();
            String method = (bestMatch != null && bestMatch.getAnalysisMethod() != null) ? bestMatch.getAnalysisMethod() : "HYBRID_AI";
            String explanation = (bestMatch != null && bestMatch.getExplanation() != null) ? bestMatch.getExplanation() : "Role compatible match.";

            String companyName = "CareerLens Partner";
            if (job.getCompanyId() != null) {
                companyName = companyRepository.findById(job.getCompanyId())
                        .map(Company::getName).orElse("CareerLens Partner");
            } else if (job.getRecruiterId() != null) {
                companyName = companyRepository.findByUserId(job.getRecruiterId())
                        .map(Company::getName).orElse("CareerLens Partner");
            }
            boolean isSaved = savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, job.getId());

            String salaryRange = job.getSalaryMin() != null
                    ? ("₹" + String.format("%,.0f", job.getSalaryMin()) + " - ₹" + String.format("%,.0f", job.getSalaryMax()))
                    : "Competitive";

            if (StringUtils.hasText(matchedRoleName)) {
                String capitalizedRole = matchedRoleName.substring(0, 1).toUpperCase() + matchedRoleName.substring(1);
                explanation = "✓ Matched resume role: " + capitalizedRole + ". " + explanation;
            }

            RecommendationResponse rec = new RecommendationResponse(
                    job.getId(),
                    job.getTitle(),
                    companyName,
                    job.getLocation(),
                    job.getWorkMode(),
                    salaryRange,
                    job.getJobType(),
                    matchScore,
                    matchedSkills,
                    missingReq,
                    missingPref,
                    explanation,
                    method,
                    false,
                    isSaved,
                    job.getCreatedAt()
            );

            // Deduplicate by job ID
            deduplicatedRecs.putIfAbsent(job.getId(), rec);
        }

        List<RecommendationResponse> list = new ArrayList<>(deduplicatedRecs.values());

        // 7. Rank eligible jobs by match score descending
        list.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        // 8. Pagination
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }

        List<RecommendationResponse> sublist = list.subList(start, end);
        return new PageImpl<>(sublist, pageable, list.size());
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }
}
