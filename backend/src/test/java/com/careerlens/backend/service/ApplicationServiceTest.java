package com.careerlens.backend.service;

import com.careerlens.backend.dto.ApplicationResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.*;
import com.careerlens.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private JobService jobService;
    @Mock private NotificationService notificationService;
    @Mock private ResumeMatchingService resumeMatchingService;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                applicationRepository, jobRepository, resumeRepository, userRepository,
                companyRepository, jobService, notificationService, resumeMatchingService
        );
    }

    @Test
    void testJobSeekerCanApplySuccess() {
        String jobSeekerId = "seeker1";
        String jobId = "job1";
        String resumeId = "resume1";

        Job job = new Job();
        job.setId(jobId);
        job.setActive(true);

        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setJobSeekerId(jobSeekerId);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)).thenReturn(Optional.of(resume));
        when(applicationRepository.findByJobIdAndJobSeekerId(jobId, jobSeekerId)).thenReturn(Optional.empty());
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> {
            Application a = i.getArgument(0);
            a.setId("app1");
            return a;
        });

        ApplicationResponse response = applicationService.applyForJob(jobSeekerId, jobId);

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void testDuplicateApplicationIsRejected() {
        String jobSeekerId = "seeker1";
        String jobId = "job1";

        Job job = new Job();
        job.setId(jobId);
        job.setActive(true);

        Resume resume = new Resume();
        resume.setId("resume1");

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)).thenReturn(Optional.of(resume));
        Application existingApp = new Application();
        existingApp.setId("realApp1");
        when(applicationRepository.findByJobIdAndJobSeekerId(jobId, jobSeekerId)).thenReturn(Optional.of(existingApp));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                applicationService.applyForJob(jobSeekerId, jobId)
        );

        assertTrue(ex.getMessage().contains("already applied"));
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testRecruiterCannotViewAnotherRecruitersApplicants() {
        String jobId = "job1";
        String unauthorizedRecruiterId = "recruiter2";

        when(jobRepository.findByIdAndRecruiterId(jobId, unauthorizedRecruiterId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                applicationService.getApplicationsForRecruiterJob(jobId, unauthorizedRecruiterId)
        );
    }

    @Test
    void testRecruiterCanUpdateApplicationStatus() {
        String recruiterId = "recruiter1";
        String applicationId = "app1";
        String jobId = "job1";

        Application app = new Application();
        app.setId(applicationId);
        app.setJobId(jobId);
        app.setStatus(ApplicationStatus.APPLIED);

        Job job = new Job();
        job.setId(jobId);
        job.setRecruiterId(recruiterId);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(jobRepository.findByIdAndRecruiterId(jobId, recruiterId)).thenReturn(Optional.of(job));
        when(applicationRepository.save(any(Application.class))).thenAnswer(i -> i.getArgument(0));

        ApplicationResponse response = applicationService.updateApplicationStatus(
                applicationId, recruiterId, ApplicationStatus.SHORTLISTED, null
        );

        assertNotNull(response);
        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    void testInvalidStatusTransitionRejected() {
        String recruiterId = "recruiter1";
        String applicationId = "app1";
        String jobId = "job1";

        Application app = new Application();
        app.setId(applicationId);
        app.setJobId(jobId);
        app.setStatus(ApplicationStatus.SELECTED); // Terminal state

        Job job = new Job();
        job.setId(jobId);
        job.setRecruiterId(recruiterId);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(app));
        when(jobRepository.findByIdAndRecruiterId(jobId, recruiterId)).thenReturn(Optional.of(job));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                applicationService.updateApplicationStatus(applicationId, recruiterId, ApplicationStatus.APPLIED, null)
        );

        assertTrue(ex.getMessage().contains("terminal state"));
    }
}
