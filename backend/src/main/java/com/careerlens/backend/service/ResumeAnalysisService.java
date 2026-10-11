package com.careerlens.backend.service;

import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.ParsingStatus;
import com.careerlens.backend.model.Resume;
import com.careerlens.backend.model.ResumeAnalysis;
import com.careerlens.backend.repository.ResumeAnalysisRepository;
import com.careerlens.backend.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.*;

@Service
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final EmbeddingService embeddingService;

    public ResumeAnalysisService(ResumeRepository resumeRepository,
                                 ResumeAnalysisRepository resumeAnalysisRepository,
                                 EmbeddingService embeddingService) {
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.embeddingService = embeddingService;
    }

    public ResumeAnalysis analyzeResume(String jobSeekerId) {
        Resume resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId).stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No resume uploaded yet. Please upload a resume first."));

        // Check cache if resume hasn't changed
        Optional<ResumeAnalysis> existingOpt = resumeAnalysisRepository.findByResumeId(resume.getId());
        if (existingOpt.isPresent()) {
            ResumeAnalysis existing = existingOpt.get();
            if (resume.getUpdatedAt() != null && existing.getAnalyzedAt() != null 
                    && !resume.getUpdatedAt().isAfter(existing.getAnalyzedAt())) {
                return existing;
            }
        }

        List<String> normalizedSkills = SkillNormalizer.normalizeList(resume.getSkills());
        List<String> education = resume.getEducation() != null ? resume.getEducation() : Collections.emptyList();
        List<String> experience = resume.getExperience() != null ? resume.getExperience() : Collections.emptyList();
        List<String> projects = resume.getProjects() != null ? resume.getProjects() : Collections.emptyList();
        List<String> certs = resume.getCertifications() != null ? resume.getCertifications() : Collections.emptyList();
        String text = resume.getExtractedText() != null ? resume.getExtractedText() : "";

        // Calculate ATS Breakdown Scores (0 - 100)
        int skillsScore = !normalizedSkills.isEmpty() ? Math.min(100, Math.max(30, normalizedSkills.size() * 25)) : 0;
        int experienceScore = !experience.isEmpty() ? Math.min(100, Math.max(40, experience.size() * 35)) : 0;
        int educationScore = !education.isEmpty() ? Math.min(100, Math.max(50, education.size() * 50)) : 0;
        
        int keywordCount = countKeywords(text);
        int keywordScore = keywordCount > 0 ? Math.min(100, Math.max(30, keywordCount * 15)) : 0;

        int structureSections = 0;
        if (StringUtils.hasText(resume.getSummary())) structureSections++;
        if (!normalizedSkills.isEmpty()) structureSections++;
        if (!experience.isEmpty()) structureSections++;
        if (!education.isEmpty()) structureSections++;
        if (!projects.isEmpty() || !certs.isEmpty()) structureSections++;
        int structureScore = Math.min(100, structureSections * 20);

        // Overall ATS Score formula
        int overallScore = (int) Math.round(
                skillsScore * 0.30 +
                experienceScore * 0.25 +
                educationScore * 0.20 +
                keywordScore * 0.15 +
                structureScore * 0.10
        );
        overallScore = Math.max(10, Math.min(100, overallScore));

        // Generate Resume Suggestions
        List<String> suggestions = new ArrayList<>();
        if (normalizedSkills.size() < 4) {
            suggestions.add("Add more specific technical skills and frameworks to improve match visibility.");
        }
        if (experience.isEmpty()) {
            suggestions.add("Include detailed work history or internship experience with measurable outcomes.");
        }
        if (education.isEmpty()) {
            suggestions.add("Specify your degree, university/college name, and graduation year clearly.");
        }
        if (!StringUtils.hasText(resume.getSummary())) {
            suggestions.add("Add a brief professional summary at the top of your resume.");
        }
        if (projects.isEmpty() && certs.isEmpty()) {
            suggestions.add("Highlight personal projects or professional certifications to demonstrate hands-on experience.");
        }

        List<Double> embedding = embeddingService.generateEmbedding(text);
        String method = embeddingService.isAvailable() ? "HYBRID_AI" : "DETERMINISTIC_FALLBACK";

        ResumeAnalysis analysis = existingOpt.orElseGet(ResumeAnalysis::new);
        analysis.setResumeId(resume.getId());
        analysis.setJobSeekerId(jobSeekerId);
        analysis.setOverallScore(overallScore);
        analysis.setSkillsScore(skillsScore);
        analysis.setExperienceScore(experienceScore);
        analysis.setEducationScore(educationScore);
        analysis.setKeywordScore(keywordScore);
        analysis.setStructureScore(structureScore);
        analysis.setExtractedSkills(normalizedSkills);
        analysis.setExtractedEducation(education);
        analysis.setExtractedExperience(experience);
        analysis.setProjects(projects);
        analysis.setCertifications(certs);
        analysis.setEmbedding(embedding);
        analysis.setSuggestions(suggestions);
        analysis.setAnalysisMethod(method);
        analysis.setAnalyzedAt(Instant.now());

        return resumeAnalysisRepository.save(analysis);
    }

    private int countKeywords(String text) {
        if (!StringUtils.hasText(text)) return 0;
        String lower = text.toLowerCase();
        List<String> keywords = Arrays.asList(
                "developed", "designed", "built", "managed", "led", "architecture", "database",
                "testing", "deployed", "optimized", "agile", "api", "framework", "cloud", "security"
        );
        int count = 0;
        for (String kw : keywords) {
            if (lower.contains(kw)) count++;
        }
        return count;
    }
}
