package com.careerlens.backend.service;

import com.careerlens.backend.model.ParsingStatus;
import com.careerlens.backend.model.Resume;
import com.careerlens.backend.model.ResumeAnalysis;
import com.careerlens.backend.repository.ResumeAnalysisRepository;
import com.careerlens.backend.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeAnalysisServiceTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private ResumeAnalysisRepository resumeAnalysisRepository;
    @Mock private EmbeddingService embeddingService;

    private ResumeAnalysisService resumeAnalysisService;

    @BeforeEach
    void setUp() {
        resumeAnalysisService = new ResumeAnalysisService(resumeRepository, resumeAnalysisRepository, embeddingService);
    }

    @Test
    void testAnalyzeResumeCalculatesRealAtsScore() {
        String jobSeekerId = "user1";
        Resume resume = new Resume(
                "res1", jobSeekerId, "resume.pdf", "stored.pdf", "application/pdf", 1024, "/path",
                "Senior Java Developer with Spring Boot and MongoDB experience.", ParsingStatus.SUCCESS,
                Arrays.asList("Java", "Spring Boot", "MongoDB", "React"),
                Arrays.asList("B.Tech in Computer Science"),
                Arrays.asList("3 years Senior Developer at Tech Corp"),
                Arrays.asList("E-Commerce Portal"), Arrays.asList("AWS Certified"),
                "Experienced backend software engineer.", Instant.now(), Instant.now()
        );

        when(resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)).thenReturn(Optional.of(resume));
        when(resumeAnalysisRepository.findByResumeId("res1")).thenReturn(Optional.empty());
        when(embeddingService.generateEmbedding(any())).thenReturn(Arrays.asList(0.1, 0.2, 0.3));
        when(embeddingService.isAvailable()).thenReturn(true);
        when(resumeAnalysisRepository.save(any(ResumeAnalysis.class))).thenAnswer(i -> i.getArgument(0));

        ResumeAnalysis result = resumeAnalysisService.analyzeResume(jobSeekerId);

        assertNotNull(result);
        assertTrue(result.getOverallScore() >= 50 && result.getOverallScore() <= 100);
        assertTrue(result.getSkillsScore() > 0);
        assertTrue(result.getEducationScore() > 0);
        assertTrue(result.getExtractedSkills().contains("Java"));
        assertEquals("HYBRID_AI", result.getAnalysisMethod());
    }
}
