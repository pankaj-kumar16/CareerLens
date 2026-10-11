package com.careerlens.backend.service;

import com.careerlens.backend.dto.CandidateRankingResponse;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.*;
import com.careerlens.backend.repository.*;
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
class ResumeMatchingServiceTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private JobRepository jobRepository;
    @Mock private MatchResultRepository matchResultRepository;
    @Mock private ResumeAnalysisService resumeAnalysisService;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmbeddingService embeddingService;

    private ResumeMatchingService resumeMatchingService;

    @BeforeEach
    void setUp() {
        resumeMatchingService = new ResumeMatchingService(
                resumeRepository, jobRepository, matchResultRepository,
                resumeAnalysisService, applicationRepository, userRepository,
                embeddingService, 0.40, 0.30, 0.15, 0.10, 0.05
        );
    }

    @Test
    void testMatchResumeToJobCalculatesBoundedScoreAndSkillGaps() {
        String resumeId = "res1";
        String jobId = "job1";

        Resume resume = new Resume(
                resumeId, "seeker1", "resume.pdf", "stored.pdf", "application/pdf", 1024, "/path",
                "Experienced Java Developer with Spring Boot and SQL skills.", ParsingStatus.SUCCESS,
                Arrays.asList("Java", "Spring Boot", "SQL"),
                Arrays.asList("B.Tech Degree"),
                Arrays.asList("3 years Engineer"),
                Arrays.asList("Project A"), Arrays.asList(),
                "Summary", Instant.now(), Instant.now()
        );

        Job job = new Job(
                jobId, "recruiter1", "comp1", "Backend Developer", "Java Spring Boot position",
                Arrays.asList("Java", "Spring Boot", "Docker"), // Required: Docker missing!
                Arrays.asList("AWS"),                           // Preferred: AWS missing!
                2, 5, "B.Tech", 100000.0, 140000.0, "Remote",
                WorkMode.REMOTE, JobType.FULL_TIME,
                Arrays.asList("Write APIs"), Arrays.asList("Java experience"), true,
                Instant.now(), Instant.now()
        );

        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setEmbedding(Arrays.asList(0.5, 0.5, 0.5));

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(matchResultRepository.findByResumeIdAndJobId(resumeId, jobId)).thenReturn(Optional.empty());
        when(resumeAnalysisService.analyzeResume("seeker1")).thenReturn(analysis);
        when(embeddingService.generateEmbedding(any())).thenReturn(Arrays.asList(0.5, 0.5, 0.5));
        when(embeddingService.isAvailable()).thenReturn(false);
        when(matchResultRepository.save(any(MatchResult.class))).thenAnswer(i -> i.getArgument(0));

        MatchResult match = resumeMatchingService.matchResumeToJob(resumeId, jobId);

        assertNotNull(match);
        assertTrue(match.getOverallScore() >= 0 && match.getOverallScore() <= 100);
        assertTrue(match.getMatchedSkills().contains("Java"));
        assertTrue(match.getMissingRequiredSkills().contains("Docker"));
        assertTrue(match.getMissingPreferredSkills().contains("AWS"));
        assertEquals("DETERMINISTIC_FALLBACK", match.getAnalysisMethod());
    }

    @Test
    void testRankCandidatesForJobRanksDescending() {
        String jobId = "job1";
        String recruiterId = "recruiter1";

        Job job = new Job();
        job.setId(jobId);
        job.setRecruiterId(recruiterId);

        Application app1 = new Application("app1", jobId, "user1", "res1", ApplicationStatus.APPLIED, Instant.now(), Instant.now(), null, null);
        Application app2 = new Application("app2", jobId, "user2", "res2", ApplicationStatus.APPLIED, Instant.now(), Instant.now(), null, null);

        Resume res1 = new Resume("res1", "user1", "res1.pdf", "stored1.pdf", "pdf", 100, "path", "text", ParsingStatus.SUCCESS, Arrays.asList("Java"), Arrays.asList(), Arrays.asList(), Arrays.asList(), Arrays.asList(), "", Instant.now(), Instant.now());
        Resume res2 = new Resume("res2", "user2", "res2.pdf", "stored2.pdf", "pdf", 100, "path", "text", ParsingStatus.SUCCESS, Arrays.asList("Java", "Spring Boot", "Docker"), Arrays.asList(), Arrays.asList(), Arrays.asList(), Arrays.asList(), "", Instant.now(), Instant.now());

        User u1 = new User("user1", "user1@test.com", "Alice Candidate", "123", Role.JOB_SEEKER, true, true);
        User u2 = new User("user2", "user2@test.com", "Bob Candidate", "456", Role.JOB_SEEKER, true, true);

        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setEmbedding(Arrays.asList(0.1, 0.2));

        when(jobRepository.findByIdAndRecruiterId(jobId, recruiterId)).thenReturn(Optional.of(job));
        when(applicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)).thenReturn(Arrays.asList(app1, app2));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findById("res1")).thenReturn(Optional.of(res1));
        when(resumeRepository.findById("res2")).thenReturn(Optional.of(res2));
        when(resumeAnalysisService.analyzeResume(any())).thenReturn(analysis);
        when(embeddingService.generateEmbedding(any())).thenReturn(Arrays.asList(0.1, 0.2));
        when(userRepository.findById("user1")).thenReturn(Optional.of(u1));
        when(userRepository.findById("user2")).thenReturn(Optional.of(u2));
        when(matchResultRepository.save(any(MatchResult.class))).thenAnswer(i -> i.getArgument(0));

        List<CandidateRankingResponse> ranked = resumeMatchingService.rankCandidatesForJob(jobId, recruiterId);

        assertNotNull(ranked);
        assertEquals(2, ranked.size());
        // Candidate Bob with more matching skills should rank higher than Alice
        assertTrue(ranked.get(0).getMatchScore() >= ranked.get(1).getMatchScore());
    }

    @Test
    void testUnauthorizedRecruiterCannotRankAnotherRecruitersCandidates() {
        String jobId = "job1";
        String unauthorizedRecruiter = "recruiter999";

        when(jobRepository.findByIdAndRecruiterId(jobId, unauthorizedRecruiter)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                resumeMatchingService.rankCandidatesForJob(jobId, unauthorizedRecruiter)
        );
    }
}
