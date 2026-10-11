package com.careerlens.backend.service;

import com.careerlens.backend.dto.RecommendationResponse;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.MatchResult;
import com.careerlens.backend.model.Resume;
import com.careerlens.backend.model.WorkMode;
import com.careerlens.backend.repository.ApplicationRepository;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.ResumeRepository;
import com.careerlens.backend.repository.SavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ResumeMatchingService resumeMatchingService;

    @InjectMocks
    private RecommendationService recommendationService;

    private Resume fullStackResume;
    private Job activeFullStackJob;
    private Job inactiveFullStackJob;
    private Job activeDataAnalystJob;

    @BeforeEach
    void setUp() {
        recommendationService.setMinScore(0.0);

        fullStackResume = new Resume();
        fullStackResume.setId("res1");
        fullStackResume.setJobSeekerId("seeker1");
        fullStackResume.setTitle("Full Stack Developer");
        fullStackResume.setSkills(List.of("Java", "React", "Node.js", "JavaScript"));
        fullStackResume.setEducation(List.of("B.Tech"));

        activeFullStackJob = new Job();
        activeFullStackJob.setId("job1");
        activeFullStackJob.setRecruiterId("rec1");
        activeFullStackJob.setTitle("full stack developer");
        activeFullStackJob.setLocation("Delhi");
        activeFullStackJob.setWorkMode(WorkMode.HYBRID);
        activeFullStackJob.setSalaryMin(500000.0);
        activeFullStackJob.setSalaryMax(700000.0);
        activeFullStackJob.setRequiredSkills(List.of("Java", "React"));
        activeFullStackJob.setActive(true);

        inactiveFullStackJob = new Job();
        inactiveFullStackJob.setId("job2");
        inactiveFullStackJob.setRecruiterId("rec1");
        inactiveFullStackJob.setTitle("Full-Stack Developer (Draft)");
        inactiveFullStackJob.setLocation("Delhi");
        inactiveFullStackJob.setActive(false); // Inactive

        activeDataAnalystJob = new Job();
        activeDataAnalystJob.setId("job3");
        activeDataAnalystJob.setRecruiterId("rec1");
        activeDataAnalystJob.setTitle("Data Analyst");
        activeDataAnalystJob.setLocation("Noida");
        activeDataAnalystJob.setActive(true);
    }

    @Test
    @DisplayName("Test G: Active published compatible job appears in recommendation API response")
    void testG_ActiveCompatibleJobAppearsInRecommendations() {
        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker1")).thenReturn(List.of(fullStackResume));
        when(jobRepository.findAll()).thenReturn(List.of(activeFullStackJob, activeDataAnalystJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        MatchResult match = new MatchResult();
        match.setId("m1");
        match.setResumeId("res1");
        match.setJobId("job1");
        match.setOverallScore(85);
        match.setMatchedSkills(List.of("Java", "React"));
        match.setExplanation("Full Stack match");

        when(resumeMatchingService.calculateMatch(fullStackResume, activeFullStackJob)).thenReturn(match);

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker1", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("job1", result.getContent().get(0).getJobId());
        assertEquals("full stack developer", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Test H: Inactive or unpublished compatible job does NOT appear in recommendations")
    void testH_InactiveJobExcludedFromRecommendations() {
        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker1")).thenReturn(List.of(fullStackResume));
        when(jobRepository.findAll()).thenReturn(List.of(inactiveFullStackJob));

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker1", 0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
}
