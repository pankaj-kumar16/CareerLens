package com.careerlens.backend.service;

import com.careerlens.backend.dto.RecruiterAnalyticsResponse;
import com.careerlens.backend.model.Application;
import com.careerlens.backend.model.ApplicationStatus;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.WorkMode;
import com.careerlens.backend.repository.ApplicationRepository;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.MatchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private MatchResultRepository matchResultRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Job job1;
    private Application app1;
    private Application app2;

    @BeforeEach
    void setUp() {
        job1 = new Job();
        job1.setId("job1");
        job1.setRecruiterId("recruiter1");
        job1.setTitle("Java Developer");
        job1.setWorkMode(WorkMode.REMOTE);
        job1.setActive(true);

        app1 = new Application("app1", "job1", "seeker1", "res1", ApplicationStatus.SHORTLISTED,
                Instant.now(), Instant.now(), null, null);

        app2 = new Application("app2", "job1", "seeker2", "res2", ApplicationStatus.SELECTED,
                Instant.now(), Instant.now(), null, null);
    }

    @Test
    void testGetRecruiterAnalytics() {
        when(jobRepository.findByRecruiterId("recruiter1")).thenReturn(List.of(job1));
        when(applicationRepository.findByJobIdIn(List.of("job1"))).thenReturn(List.of(app1, app2));
        when(matchResultRepository.findByJobId("job1")).thenReturn(List.of());

        RecruiterAnalyticsResponse response = analyticsService.getRecruiterAnalytics("recruiter1", 30);

        assertNotNull(response);
        assertEquals(1, response.getActiveJobs());
        assertEquals(2, response.getTotalApplicants());
        assertEquals(1, response.getShortlisted());
        assertEquals(1, response.getSelected());
        assertEquals(0, response.getInterviews());
        assertEquals(0, response.getRejected());

        assertEquals(1, response.getJobPerformance().size());
        assertEquals("job1", response.getJobPerformance().get(0).getJobId());
        assertEquals(2, response.getJobPerformance().get(0).getTotalApplicants());
    }

    @Test
    void testGetRecruiterAnalyticsNoJobs() {
        when(jobRepository.findByRecruiterId("recruiter1")).thenReturn(List.of());

        RecruiterAnalyticsResponse response = analyticsService.getRecruiterAnalytics("recruiter1", 30);

        assertNotNull(response);
        assertEquals(0, response.getActiveJobs());
        assertEquals(0, response.getTotalApplicants());
        assertTrue(response.getJobPerformance().isEmpty());
    }
}
