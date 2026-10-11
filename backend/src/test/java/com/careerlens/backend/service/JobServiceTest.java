package com.careerlens.backend.service;

import com.careerlens.backend.dto.JobRequest;
import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, companyRepository);
    }

    @Test
    void testRecruiterCanCreateJob() {
        String recruiterId = "recruiter1";
        JobRequest request = new JobRequest(
                "Senior Java Engineer",
                "Great role in scalable backend design",
                List.of("Java", "Spring Boot"),
                List.of("MongoDB"),
                3, 5, "B.Tech", 120000.0, 160000.0,
                "Remote", WorkMode.REMOTE, JobType.FULL_TIME,
                List.of("Design APIs"), List.of("3+ yrs Java"), true
        );

        Company company = new Company("comp1", "Tech Corp", "logo.png", "Desc", "IT", "NYC", "http", "100-500", 2020, recruiterId, Instant.now(), Instant.now());
        when(companyRepository.findByUserId(recruiterId)).thenReturn(Optional.of(company));
        when(companyRepository.findById("comp1")).thenReturn(Optional.of(company));
        when(jobRepository.save(any(Job.class))).thenAnswer(i -> {
            Job j = i.getArgument(0);
            j.setId("job123");
            return j;
        });

        JobResponse response = jobService.createJob(recruiterId, request);

        assertNotNull(response);
        assertEquals("Senior Java Engineer", response.getTitle());
        assertEquals("Tech Corp", response.getCompanyName());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void testRecruiterCannotModifyAnotherRecruitersJob() {
        String ownerRecruiterId = "recruiter1";
        String unauthorizedRecruiterId = "recruiter2";
        String jobId = "job123";

        JobRequest updateReq = new JobRequest();
        updateReq.setTitle("Hacked Title");

        when(jobRepository.findByIdAndRecruiterId(jobId, unauthorizedRecruiterId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                jobService.updateJob(jobId, unauthorizedRecruiterId, updateReq)
        );

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void testPublicUsersCanViewActiveJobs() {
        Job activeJob = new Job(
                "job123", "recruiter1", "comp1", "Frontend Dev", "React job",
                List.of("React"), List.of("Tailwind"), 1, 3, "B.S.",
                80000.0, 110000.0, "NY", WorkMode.HYBRID, JobType.FULL_TIME,
                List.of(), List.of(), true, Instant.now(), Instant.now()
        );

        when(jobRepository.findByIdAndActiveTrue("job123")).thenReturn(Optional.of(activeJob));

        JobResponse response = jobService.getPublicJobById("job123");

        assertNotNull(response);
        assertEquals("Frontend Dev", response.getTitle());
        assertTrue(response.isActive());
    }
}
