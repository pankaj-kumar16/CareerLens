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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

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
     * Recommended job eligibility pipeline:
     * 1. Load candidate's uploaded resumes.
     * 2. Detect candidate roles and technical skills across all uploaded versions.
     * 3. Fetch active published jobs from repository.
     * 4. Exclude jobs where candidate already applied.
     * 5. Filter through role compatibility and skill overlap.
     * 6. Calculate match score, matched skills, and explanation.
     * 7. Rank and paginate up to requested size (or Top 5).
     */
    public Page<RecommendationResponse> getRecommendationsForJobSeeker(String jobSeekerId, int page, int size) {
        log.info("[RecommendationPipeline] Starting recommendation evaluation for jobSeekerId={}", jobSeekerId);

        // 1. Load resumes for this candidate
        List<Resume> allResumes = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
        if (allResumes.isEmpty()) {
            log.warn("[RecommendationPipeline] No resumes found for seekerId={}", jobSeekerId);
            throw new BadRequestException("Please upload a resume first to receive personalized AI job recommendations.");
        }

        // 2. Extract roles & skills across all resume versions
        Map<String, Set<String>> rolesByResumeId = new LinkedHashMap<>();
        Set<String> allCandidateRoles = new LinkedHashSet<>();
        Set<String> allCandidateSkills = new LinkedHashSet<>();

        for (Resume resume : allResumes) {
            Set<String> detectedRoles = RoleDetectorUtils.detectAllRoles(resume);
            for (String r : detectedRoles) {
                if (StringUtils.hasText(r)) {
                    allCandidateRoles.add(r.toLowerCase());
                    rolesByResumeId.computeIfAbsent(resume.getId(), k -> new LinkedHashSet<>()).add(r.toLowerCase());
                }
            }
            if (resume.getSkills() != null) {
                for (String s : resume.getSkills()) {
                    if (StringUtils.hasText(s)) {
                        allCandidateSkills.add(s.toLowerCase().trim());
                    }
                }
            }
        }

        // Identify default resume
        Resume defaultResume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                .or(() -> allResumes.stream().findFirst())
                .orElse(allResumes.get(0));

        log.info("[RecommendationPipeline] SeekerId={} has {} resume(s). Default resumeId={}. Roles extracted={}. Skills count={}",
                jobSeekerId, allResumes.size(), defaultResume.getId(), allCandidateRoles, allCandidateSkills.size());

        // 3. Fetch active jobs
        List<Job> allActiveJobs = jobRepository.findAll().stream()
                .filter(j -> j != null && j.isActive())
                .toList();

        log.info("[RecommendationPipeline] Found {} active jobs in database for evaluation.", allActiveJobs.size());

        Map<String, RecommendationResponse> deduplicatedRecs = new LinkedHashMap<>();
        int skippedAlreadyApplied = 0;
        int skippedIncompatibleRole = 0;

        for (Job job : allActiveJobs) {
            // Check if already applied
            boolean hasRealApplication = applicationRepository.findByJobIdAndJobSeekerId(job.getId(), jobSeekerId)
                    .map(app -> app.getId() != null && !app.getId().startsWith("discovery_"))
                    .orElse(false);
            if (hasRealApplication) {
                skippedAlreadyApplied++;
                log.debug("[RecommendationPipeline] Job id={} ('{}') skipped: already applied.", job.getId(), job.getTitle());
                continue;
            }

            // Role & Skill Compatibility check
            boolean compatible = allCandidateRoles.isEmpty() || RoleCompatibilityUtils.isCompatible(job, allCandidateRoles, allCandidateSkills);
            if (!compatible) {
                skippedIncompatibleRole++;
                log.debug("[RecommendationPipeline] Job id={} ('{}') rejected: incompatible with roles {} and skills count {}",
                        job.getId(), job.getTitle(), allCandidateRoles, allCandidateSkills.size());
                continue;
            }

            // Calculate match score across candidate resumes
            MatchResult bestMatch = null;
            String matchedRoleName = null;

            for (Resume resume : allResumes) {
                Set<String> resumeRoles = rolesByResumeId.getOrDefault(resume.getId(), Set.of());
                Set<String> resumeSkills = resume.getSkills() != null
                        ? new LinkedHashSet<>(resume.getSkills().stream().filter(StringUtils::hasText).map(String::toLowerCase).toList())
                        : Collections.emptySet();

                if (resumeRoles.isEmpty() || RoleCompatibilityUtils.isCompatible(job, resumeRoles, resumeSkills)) {
                    try {
                        MatchResult match = resumeMatchingService.calculateMatch(resume, job);
                        if (bestMatch == null || (match != null && match.getOverallScore() > bestMatch.getOverallScore())) {
                            bestMatch = match;
                            matchedRoleName = resumeRoles.isEmpty() ? null : resumeRoles.iterator().next();
                        }
                    } catch (Exception e) {
                        log.warn("[RecommendationPipeline] Error calculating match for resumeId={} and jobId={}: {}",
                                resume.getId(), job.getId(), e.getMessage());
                    }
                }
            }

            // Fallback match with default resume if needed
            if (bestMatch == null) {
                try {
                    bestMatch = resumeMatchingService.calculateMatch(defaultResume, job);
                } catch (Exception e) {
                    log.warn("[RecommendationPipeline] Error calculating fallback match for default resumeId={} and jobId={}: {}",
                            defaultResume.getId(), job.getId(), e.getMessage());
                }
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

            log.info("[RecommendationPipeline] Eligible job matched: id={}, title='{}', score={}", job.getId(), job.getTitle(), matchScore);
            deduplicatedRecs.putIfAbsent(job.getId(), rec);
        }

        List<RecommendationResponse> list = new ArrayList<>(deduplicatedRecs.values());

        // Sort by match score descending
        list.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));

        // Pagination
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<RecommendationResponse> sublist = (start > list.size()) ? List.of() : list.subList(start, end);

        log.info("[RecommendationPipeline] Finished for seekerId={}: totalEvaluated={}, skippedApplied={}, skippedIncompatible={}, totalEligible={}, returningCount={}",
                jobSeekerId, allActiveJobs.size(), skippedAlreadyApplied, skippedIncompatibleRole, list.size(), sublist.size());

        return new PageImpl<>(sublist, pageable, list.size());
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }
}
