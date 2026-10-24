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

import java.util.ArrayList;
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

    @Test
    @DisplayName("Test I: Candidate with Software Engineer title and React + Java skills is recommended Full Stack Developer job")
    void testI_SoftwareEngineerWithFullStackSkillsMatchesFullStackJob() {
        Resume seResume = new Resume();
        seResume.setId("res2");
        seResume.setJobSeekerId("seeker2");
        seResume.setTitle("Software Engineer");
        seResume.setSkills(List.of("React", "Java", "Spring Boot", "HTML", "CSS"));

        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker2")).thenReturn(List.of(seResume));
        when(jobRepository.findAll()).thenReturn(List.of(activeFullStackJob, activeDataAnalystJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        MatchResult match = new MatchResult();
        match.setId("m2");
        match.setResumeId("res2");
        match.setJobId("job1");
        match.setOverallScore(90);
        match.setMatchedSkills(List.of("Java", "React"));

        when(resumeMatchingService.calculateMatch(seResume, activeFullStackJob)).thenReturn(match);

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker2", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("job1", result.getContent().get(0).getJobId());
        assertEquals("full stack developer", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("Scenario 2: React Developer resume is recommended React.js Developer job")
    void testScenario2_ReactDeveloperRecommendedReactJob() {
        Resume reactResume = new Resume();
        reactResume.setId("res-react");
        reactResume.setJobSeekerId("seeker-react");
        reactResume.setTitle("React Developer");
        reactResume.setSkills(List.of("React", "JavaScript", "HTML", "CSS", "Tailwind CSS"));

        Job reactJob = new Job();
        reactJob.setId("job-react");
        reactJob.setTitle("Senior React.js Developer");
        reactJob.setActive(true);
        reactJob.setRequiredSkills(List.of("React", "JavaScript"));

        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker-react")).thenReturn(List.of(reactResume));
        when(jobRepository.findAll()).thenReturn(List.of(reactJob, activeDataAnalystJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        MatchResult match = new MatchResult();
        match.setOverallScore(88);
        match.setMatchedSkills(List.of("React", "JavaScript"));
        when(resumeMatchingService.calculateMatch(reactResume, reactJob)).thenReturn(match);

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker-react", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("job-react", result.getContent().get(0).getJobId());
    }

    @Test
    @DisplayName("Scenario 3: Python Developer resume is recommended Django/FastAPI job")
    void testScenario3_PythonDeveloperRecommendedPythonJob() {
        Resume pythonResume = new Resume();
        pythonResume.setId("res-py");
        pythonResume.setJobSeekerId("seeker-py");
        pythonResume.setTitle("Python Developer");
        pythonResume.setSkills(List.of("Python", "Django", "PostgreSQL"));

        Job pythonJob = new Job();
        pythonJob.setId("job-py");
        pythonJob.setTitle("Django Backend Developer");
        pythonJob.setActive(true);
        pythonJob.setRequiredSkills(List.of("Python", "Django"));

        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker-py")).thenReturn(List.of(pythonResume));
        when(jobRepository.findAll()).thenReturn(List.of(pythonJob, activeDataAnalystJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        MatchResult match = new MatchResult();
        match.setOverallScore(92);
        when(resumeMatchingService.calculateMatch(pythonResume, pythonJob)).thenReturn(match);

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker-py", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("job-py", result.getContent().get(0).getJobId());
    }

    @Test
    @DisplayName("Scenario 4: DevOps resume is recommended job with leading space & unicode en-dash")
    void testScenario4_DevOpsDeveloperRecommendedDevOpsJob() {
        Resume devopsResume = new Resume();
        devopsResume.setId("res-devops");
        devopsResume.setJobSeekerId("seeker-devops");
        devopsResume.setOriginalFileName("Ashutosh_Pandey_resume.pdf");
        devopsResume.setSkills(List.of("Python", "MySQL", "Docker", "Kubernetes", "AWS", "Linux"));

        Job devopsJob = new Job();
        devopsJob.setId("job-devops");
        devopsJob.setTitle(" DevOps Engineer – Cloud Infrastructure & Automation");
        devopsJob.setActive(true);
        devopsJob.setRequiredSkills(List.of("AWS", "Kubernetes", "Docker", "Linux"));

        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker-devops")).thenReturn(List.of(devopsResume));
        when(jobRepository.findAll()).thenReturn(List.of(devopsJob, activeDataAnalystJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        MatchResult match = new MatchResult();
        match.setOverallScore(86);
        when(resumeMatchingService.calculateMatch(devopsResume, devopsJob)).thenReturn(match);

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker-devops", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("job-devops", result.getContent().get(0).getJobId());
    }

    @Test
    @DisplayName("Scenario 5: Resume with missing optional fields does not fail")
    void testScenario5_MissingOptionalFields() {
        Resume minimalResume = new Resume();
        minimalResume.setId("res-min");
        minimalResume.setJobSeekerId("seeker-min");
        minimalResume.setTitle("Full Stack Developer");
        minimalResume.setSkills(null); // null skills
        minimalResume.setEducation(null); // null education
        minimalResume.setExperience(null); // null experience
        minimalResume.setSummary(null); // null summary

        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker-min")).thenReturn(List.of(minimalResume));
        when(jobRepository.findAll()).thenReturn(List.of(activeFullStackJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker-min", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Scenario 6: Candidate with no relevant active jobs returns genuine empty page")
    void testScenario6_NoRelevantActiveJobsReturnsEmpty() {
        Resume dataResume = new Resume();
        dataResume.setId("res-data");
        dataResume.setJobSeekerId("seeker-data");
        dataResume.setTitle("Data Analyst");
        dataResume.setSkills(List.of("SQL", "Excel", "Tableau"));

        // Only Full Stack jobs exist
        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker-data")).thenReturn(List.of(dataResume));
        when(jobRepository.findAll()).thenReturn(List.of(activeFullStackJob));
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker-data", 0, 5);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Scenario 8: When more than 5 eligible jobs exist, returns best 5 ranked by score")
    void testScenario8_MoreThanFiveEligibleJobsReturnsBestFive() {
        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc("seeker1")).thenReturn(List.of(fullStackResume));

        List<Job> multipleJobs = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            Job j = new Job();
            j.setId("job-" + i);
            j.setTitle("Full Stack Developer " + i);
            j.setActive(true);
            multipleJobs.add(j);

            MatchResult m = new MatchResult();
            m.setOverallScore(60 + i * 4); // scores: 64, 68, 72, 76, 80, 84, 88, 92
            lenient().when(resumeMatchingService.calculateMatch(fullStackResume, j)).thenReturn(m);
        }

        when(jobRepository.findAll()).thenReturn(multipleJobs);
        when(applicationRepository.findByJobIdAndJobSeekerId(any(), any())).thenReturn(Optional.empty());

        Page<RecommendationResponse> result = recommendationService.getRecommendationsForJobSeeker("seeker1", 0, 5);

        assertNotNull(result);
        assertEquals(5, result.getContent().size());
        // Best job (score 92) must be first
        assertEquals("job-8", result.getContent().get(0).getJobId());
        assertEquals(92.0, result.getContent().get(0).getMatchScore());
    }
}
