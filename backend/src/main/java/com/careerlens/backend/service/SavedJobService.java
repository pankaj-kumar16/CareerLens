package com.careerlens.backend.service;

import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.dto.SavedJobResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.SavedJob;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.SavedJobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobService jobService;

    public SavedJobService(SavedJobRepository savedJobRepository,
                           JobRepository jobRepository,
                           JobService jobService) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
    }

    public SavedJobResponse saveJob(String jobSeekerId, String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found."));

        if (!job.isActive()) {
            throw new BadRequestException("Cannot save an inactive job.");
        }

        if (savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId)) {
            throw new BadRequestException("Job is already saved.");
        }

        SavedJob savedJob = new SavedJob(null, jobSeekerId, jobId, Instant.now());

        SavedJob saved = savedJobRepository.save(savedJob);
        JobResponse jobResponse = jobService.mapToResponse(job);
        return SavedJobResponse.fromEntity(saved, jobResponse);
    }

    public void unsaveJob(String jobSeekerId, String jobId) {
        SavedJob savedJob = savedJobRepository.findByJobSeekerIdAndJobId(jobSeekerId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job entry not found."));
        savedJobRepository.delete(savedJob);
    }

    public List<SavedJobResponse> getSavedJobs(String jobSeekerId) {
        List<SavedJob> savedJobs = savedJobRepository.findByJobSeekerIdOrderBySavedAtDesc(jobSeekerId);
        return savedJobs.stream().map(saved -> {
            JobResponse jobResponse = null;
            try {
                Job job = jobRepository.findById(saved.getJobId()).orElse(null);
                if (job != null) {
                    jobResponse = jobService.mapToResponse(job);
                }
            } catch (Exception ignored) {}
            return SavedJobResponse.fromEntity(saved, jobResponse);
        }).toList();
    }

    public boolean isJobSaved(String jobSeekerId, String jobId) {
        if (jobSeekerId == null || jobId == null) return false;
        return savedJobRepository.existsByJobSeekerIdAndJobId(jobSeekerId, jobId);
    }
}
