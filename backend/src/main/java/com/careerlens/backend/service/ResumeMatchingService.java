package com.careerlens.backend.service;

import com.careerlens.backend.dto.ApplicationResponse;
import com.careerlens.backend.dto.CandidateRankingResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.*;
import com.careerlens.backend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

@Service
public class ResumeMatchingService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final MatchResultRepository matchResultRepository;
    private final ResumeAnalysisService resumeAnalysisService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;

    // Configurable Scoring Weights (centralized)
    private final double weightSkill;
    private final double weightSemantic;
    private final double weightExperience;
    private final double weightEducation;
    private final double weightRequirement;

    public ResumeMatchingService(
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            MatchResultRepository matchResultRepository,
            ResumeAnalysisService resumeAnalysisService,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            EmbeddingService embeddingService,
            @Value("${matching.weight.skill:0.40}") double weightSkill,
            @Value("${matching.weight.semantic:0.30}") double weightSemantic,
            @Value("${matching.weight.experience:0.15}") double weightExperience,
            @Value("${matching.weight.education:0.10}") double weightEducation,
            @Value("${matching.weight.requirement:0.05}") double weightRequirement
    ) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.matchResultRepository = matchResultRepository;
        this.resumeAnalysisService = resumeAnalysisService;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
        this.weightSkill = weightSkill;
        this.weightSemantic = weightSemantic;
        this.weightExperience = weightExperience;
        this.weightEducation = weightEducation;
        this.weightRequirement = weightRequirement;
    }

    public MatchResult calculateMatch(Resume resume, Job job) {
        if (resume == null || job == null) {
            throw new IllegalArgumentException("Resume and Job must not be null.");
        }
        return matchResumeToJob(resume.getId(), job.getId());
    }

    public MatchResult matchJobSeekerToJob(String jobSeekerId, String jobId) {
        // Use all resumes and pick the one with the best match score for this job
        List<Resume> allResumes = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
        if (allResumes.isEmpty()) {
            throw new BadRequestException("Please upload a resume first to view match details.");
        }

        MatchResult best = null;
        for (Resume resume : allResumes) {
            try {
                MatchResult match = matchResumeToJob(resume.getId(), jobId);
                if (best == null || match.getOverallScore() > best.getOverallScore()) {
                    best = match;
                }
            } catch (Exception ignored) {}
        }

        if (best == null) {
            // Fallback: use first resume
            return matchResumeToJob(allResumes.get(0).getId(), jobId);
        }
        return best;
    }

    public MatchResult matchResumeToJob(String resumeId, String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found."));

        // Check cached match result (re-calculate if job OR resume was updated after match cached)
        Optional<MatchResult> existingOpt = matchResultRepository.findByResumeIdAndJobId(resumeId, jobId);
        if (existingOpt.isPresent()) {
            MatchResult existing = existingOpt.get();
            boolean jobNotUpdated = job.getUpdatedAt() == null || existing.getUpdatedAt() == null || !job.getUpdatedAt().isAfter(existing.getUpdatedAt());
            boolean resumeNotUpdated = resume.getUpdatedAt() == null || existing.getUpdatedAt() == null || !resume.getUpdatedAt().isAfter(existing.getUpdatedAt());
            if (jobNotUpdated && resumeNotUpdated) {
                return existing;
            }
        }

        // 1. Skill Extraction & Normalization
        List<String> candidateSkills = SkillNormalizer.normalizeList(resume.getSkills());
        List<String> requiredSkills = SkillNormalizer.normalizeList(job.getRequiredSkills());
        List<String> preferredSkills = SkillNormalizer.normalizeList(job.getPreferredSkills());

        // Skill Gap Analysis
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingRequiredSkills = new ArrayList<>();
        List<String> missingPreferredSkills = new ArrayList<>();

        for (String req : requiredSkills) {
            if (candidateSkills.contains(req)) {
                matchedSkills.add(req);
            } else {
                missingRequiredSkills.add(req);
            }
        }

        for (String pref : preferredSkills) {
            if (candidateSkills.contains(pref)) {
                if (!matchedSkills.contains(pref)) {
                    matchedSkills.add(pref);
                }
            } else {
                missingPreferredSkills.add(pref);
            }
        }

        // 2. Calculate Skill Match Score (0 - 100)
        double reqScore = requiredSkills.isEmpty() ? 100.0 : ((double) (requiredSkills.size() - missingRequiredSkills.size()) / requiredSkills.size()) * 100.0;
        double prefScore = preferredSkills.isEmpty() ? 100.0 : ((double) (preferredSkills.size() - missingPreferredSkills.size()) / preferredSkills.size()) * 100.0;
        int skillScore = (int) Math.round(reqScore * 0.75 + prefScore * 0.25);

        // 3. Calculate Experience Match Score (0 - 100)
        int expScore = 70; // baseline
        int candidateExpEntries = resume.getExperience() != null ? resume.getExperience().size() : 0;
        int minReqExp = job.getMinimumExperience() != null ? job.getMinimumExperience() : 0;
        if (candidateExpEntries >= minReqExp) {
            expScore = Math.min(100, 75 + (candidateExpEntries - minReqExp) * 5);
        } else {
            expScore = Math.max(30, 70 - (minReqExp - candidateExpEntries) * 10);
        }

        // 4. Calculate Education Match Score (0 - 100)
        int eduScore = 75;
        if (StringUtils.hasText(job.getEducation()) && resume.getEducation() != null) {
            String reqEduLower = job.getEducation().toLowerCase();
            for (String edu : resume.getEducation()) {
                if (edu.toLowerCase().contains(reqEduLower) || reqEduLower.contains(edu.toLowerCase())) {
                    eduScore = 95;
                    break;
                }
            }
        }

        // 5. Requirement / Keyword Match Score
        int reqKeywordScore = 80;
        if (job.getRequirements() != null && !job.getRequirements().isEmpty()) {
            int matchedReqs = 0;
            String textLower = resume.getExtractedText() != null ? resume.getExtractedText().toLowerCase() : "";
            for (String req : job.getRequirements()) {
                if (textLower.contains(req.toLowerCase())) {
                    matchedReqs++;
                }
            }
            reqKeywordScore = (int) Math.round(((double) matchedReqs / job.getRequirements().size()) * 100.0);
            reqKeywordScore = Math.max(40, reqKeywordScore);
        }

        // 6. Semantic Similarity Score via Embeddings & Cosine Similarity
        ResumeAnalysis resumeAnalysis = resumeAnalysisService.analyzeResume(resume.getJobSeekerId());
        List<Double> resumeEmbedding = resumeAnalysis.getEmbedding();

        String jobText = job.getTitle() + " " + job.getDescription() + " " + String.join(" ", job.getRequiredSkills());
        List<Double> jobEmbedding = embeddingService.generateEmbedding(jobText);

        double cosineScore = CosineSimilarityUtils.calculateSimilarityScore(resumeEmbedding, jobEmbedding);
        int semanticScore = (int) Math.round(cosineScore);

        // 7. Calculate Final Hybrid Match Score (Bounded 0 - 100)
        int overallScore = (int) Math.round(
                skillScore * weightSkill +
                semanticScore * weightSemantic +
                expScore * weightExperience +
                eduScore * weightEducation +
                reqKeywordScore * weightRequirement
        );

        // Title & Domain Match Boost
        String resTitleNorm = (StringUtils.hasText(resume.getTitle()) ? resume.getTitle() : "") + " " + (StringUtils.hasText(resume.getOriginalFileName()) ? resume.getOriginalFileName() : "");
        resTitleNorm = resTitleNorm.toLowerCase().replaceAll("[_\\-]", " ");
        String jobTitleNorm = (StringUtils.hasText(job.getTitle()) ? job.getTitle() : "").toLowerCase().replaceAll("[_\\-]", " ");

        if (StringUtils.hasText(jobTitleNorm)) {
            String[] jobTitleWords = jobTitleNorm.split("\\s+");
            for (String word : jobTitleWords) {
                if (word.length() >= 4 && resTitleNorm.contains(word)) {
                    overallScore = Math.min(100, overallScore + 20);
                    break;
                }
            }
        }

        overallScore = Math.max(0, Math.min(100, overallScore));

        // 8. Generate Match Explanation
        StringBuilder explanation = new StringBuilder();
        if (overallScore >= 85) {
            explanation.append("Strong match! Candidate possesses key required skills (").append(String.join(", ", matchedSkills)).append("). ");
        } else if (overallScore >= 70) {
            explanation.append("Good candidate match with core domain competencies. ");
        } else {
            explanation.append("Moderate match. Candidate has foundational skills but noticeable skill gaps. ");
        }

        if (!missingRequiredSkills.isEmpty()) {
            explanation.append("Main skill gap: Missing required skills [").append(String.join(", ", missingRequiredSkills)).append("]. ");
        }
        if (!missingPreferredSkills.isEmpty()) {
            explanation.append("Missing preferred skills [").append(String.join(", ", missingPreferredSkills)).append("].");
        }

        String method = embeddingService.isAvailable() ? "HYBRID_AI" : "DETERMINISTIC_FALLBACK";

        MatchResult matchResult = existingOpt.orElseGet(MatchResult::new);
        matchResult.setResumeId(resumeId);
        matchResult.setJobSeekerId(resume.getJobSeekerId());
        matchResult.setJobId(jobId);
        matchResult.setOverallScore(overallScore);
        matchResult.setSkillScore(skillScore);
        matchResult.setSemanticScore(semanticScore);
        matchResult.setExperienceScore(expScore);
        matchResult.setEducationScore(eduScore);
        matchResult.setRequirementScore(reqKeywordScore);
        matchResult.setMatchedSkills(matchedSkills);
        matchResult.setMissingRequiredSkills(missingRequiredSkills);
        matchResult.setMissingPreferredSkills(missingPreferredSkills);
        matchResult.setExplanation(explanation.toString().trim());
        matchResult.setAnalysisMethod(method);
        matchResult.setUpdatedAt(Instant.now());
        if (matchResult.getCreatedAt() == null) {
            matchResult.setCreatedAt(Instant.now());
        }

        return matchResultRepository.save(matchResult);
    }

    public List<CandidateRankingResponse> rankCandidatesForJob(String jobId, String recruiterId) {
        Job job = jobRepository.findByIdAndRecruiterId(jobId, recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied."));

        List<Application> existingApplications = applicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId());
        Map<String, Application> appBySeekerId = new LinkedHashMap<>();
        for (Application app : existingApplications) {
            if (StringUtils.hasText(app.getJobSeekerId())) {
                appBySeekerId.putIfAbsent(app.getJobSeekerId(), app);
            }
        }

        // Collect all job seeker IDs from existing applications and resume uploads in platform
        Set<String> allSeekerIds = new LinkedHashSet<>(appBySeekerId.keySet());

        List<Resume> allResumes = resumeRepository.findAll();
        for (Resume r : allResumes) {
            if (StringUtils.hasText(r.getJobSeekerId())) {
                allSeekerIds.add(r.getJobSeekerId());
            }
        }

        List<CandidateRankingResponse> rankings = new ArrayList<>();

        for (String jobSeekerId : allSeekerIds) {
            Application app = appBySeekerId.get(jobSeekerId);
            Resume resume = null;

            if (app != null && StringUtils.hasText(app.getResumeId())) {
                resume = resumeRepository.findById(app.getResumeId()).orElse(null);
            }

            if (resume == null) {
                resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                        .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId).stream().findFirst())
                        .orElse(null);
            }

            if (app == null) {
                // Candidate has uploaded a resume on the platform but hasn't explicitly applied yet.
                // Construct in-memory discovery application response without polluting applicationRepository!
                String virtualAppId = "discovery_" + jobSeekerId + "_" + jobId;
                app = new Application(
                        virtualAppId,
                        jobId,
                        jobSeekerId,
                        resume != null ? resume.getId() : null,
                        ApplicationStatus.APPLIED,
                        Instant.now(),
                        Instant.now(),
                        null,
                        null
                );
            } else if (app != null && app.getId() != null && !app.getId().startsWith("discovery_") && resume != null && (app.getResumeId() == null || !app.getResumeId().equals(resume.getId()))) {
                app.setResumeId(resume.getId());
                try {
                    applicationRepository.save(app);
                } catch (Exception ignored) {}
            }

            MatchResult match = null;
            if (resume != null) {
                try {
                    match = matchResumeToJob(resume.getId(), jobId);
                } catch (Exception ignored) {}
            }

            if (match == null) {
                match = new MatchResult();
                match.setResumeId(resume != null ? resume.getId() : (app != null ? app.getResumeId() : null));
                match.setJobSeekerId(jobSeekerId);
                match.setJobId(jobId);
                match.setOverallScore(0);
                match.setSkillScore(0);
                match.setSemanticScore(0);
                match.setExperienceScore(0);
                match.setEducationScore(0);
                match.setRequirementScore(0);
                match.setMatchedSkills(List.of());
                match.setMissingRequiredSkills(job.getRequiredSkills() != null ? job.getRequiredSkills() : List.of());
                match.setMissingPreferredSkills(job.getPreferredSkills() != null ? job.getPreferredSkills() : List.of());
                match.setExplanation(resume == null ? "Candidate has not uploaded a resume." : "Match calculation pending.");
            }

            // Auto-shortlist real candidates with 90%+ match score if currently APPLIED (exclude virtual discovery apps)
            if (app != null && app.getId() != null && !app.getId().startsWith("discovery_") && app.getStatus() == ApplicationStatus.APPLIED && match.getOverallScore() >= 90) {
                app.setStatus(ApplicationStatus.SHORTLISTED);
                app.setUpdatedAt(Instant.now());
                try {
                    applicationRepository.save(app);
                } catch (Exception ignored) {}
            }

            String candidateName = "Candidate";
            String email = null;
            String phone = null;
            User user = userRepository.findById(jobSeekerId).orElse(null);
            if (user != null) {
                candidateName = user.getFullName();
                email = user.getEmail();
                phone = user.getPhone();
            }

            List<String> applicantSkills = new ArrayList<>();
            List<String> applicantEducation = new ArrayList<>();
            String originalFileName = "candidate_resume.pdf";

            if (resume != null) {
                if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
                    applicantSkills.addAll(resume.getSkills());
                }
                if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                    applicantEducation.addAll(resume.getEducation());
                }
                if (StringUtils.hasText(resume.getOriginalFileName())) {
                    originalFileName = resume.getOriginalFileName();
                }

                if (applicantSkills.isEmpty() || applicantEducation.isEmpty()) {
                    try {
                        ResumeAnalysis analysis = resumeAnalysisService.analyzeResume(resume.getJobSeekerId());
                        if (applicantSkills.isEmpty() && analysis.getExtractedSkills() != null) {
                            applicantSkills.addAll(analysis.getExtractedSkills());
                        }
                        if (applicantEducation.isEmpty() && analysis.getExtractedEducation() != null) {
                            applicantEducation.addAll(analysis.getExtractedEducation());
                        }
                    } catch (Exception ignored) {}
                }
            }

            CandidateRankingResponse res = new CandidateRankingResponse(
                    app != null ? app.getId() : null,
                    jobSeekerId,
                    resume != null ? resume.getId() : (app != null ? app.getResumeId() : null),
                    candidateName,
                    email,
                    phone,
                    app != null ? app.getStatus() : ApplicationStatus.APPLIED,
                    match.getOverallScore(),
                    match.getMatchedSkills(),
                    match.getMissingRequiredSkills(),
                    match.getExplanation(),
                    app != null && app.getAppliedAt() != null ? app.getAppliedAt() : Instant.now()
            );

            res.setOverallMatchScore(match.getOverallScore());
            res.setSkillScore(match.getSkillScore());
            res.setSemanticScore(match.getSemanticScore());
            res.setExperienceScore(match.getExperienceScore());
            res.setEducationScore(match.getEducationScore());
            res.setApplicantSkills(applicantSkills);
            res.setApplicantEducation(applicantEducation);
            res.setResumeOriginalFileName(originalFileName);

            rankings.add(res);
        }

        // Rank candidates descending by actual match score; secondary sort by candidate name
        rankings.sort((c1, c2) -> {
            int scoreCompare = Integer.compare(c2.getMatchScore(), c1.getMatchScore());
            if (scoreCompare != 0) return scoreCompare;
            return c1.getCandidateName().compareToIgnoreCase(c2.getCandidateName());
        });

        return rankings;
    }
}
