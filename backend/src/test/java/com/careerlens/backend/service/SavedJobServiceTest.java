package com.careerlens.backend.service;

import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.dto.SavedJobResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.SavedJob;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.SavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock private SavedJobRepository savedJobRepository;
    @Mock private JobRepository jobRepository;
    @Mock private JobService jobService;

    private SavedJobService savedJobService;

    @BeforeEach
    void setUp() {
        savedJobService = new SavedJobService(savedJobRepository, jobRepository, jobService);
    }

    @Test
    void testSaveJobSuccess() {
        String jobSeekerId = "seeker1";
        String jobId = "job1";

        Job job = new Job();
        job.setId(jobId);
        job.setActive(true);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId)).thenReturn(false);
        when(savedJobRepository.save(any(SavedJob.class))).thenAnswer(i -> {
            SavedJob sj = i.getArgument(0);
            sj.setId("saved1");
            return sj;
        });
        when(jobService.mapToResponse(job)).thenReturn(new JobResponse());

        SavedJobResponse response = savedJobService.saveJob(jobSeekerId, jobId);

        assertNotNull(response);
        assertEquals(jobId, response.getJobId());
        assertEquals(jobSeekerId, response.getJobSeekerId());
        verify(savedJobRepository, times(1)).save(any(SavedJob.class));
    }

    @Test
    void testDuplicateSavedJobIsRejected() {
        String jobSeekerId = "seeker1";
        String jobId = "job1";

        Job job = new Job();
        job.setId(jobId);
        job.setActive(true);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                savedJobService.saveJob(jobSeekerId, jobId)
        );

        assertTrue(ex.getMessage().contains("already saved"));
        verify(savedJobRepository, never()).save(any(SavedJob.class));
    }
}
