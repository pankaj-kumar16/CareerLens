package com.careerlens.backend.service;

import com.careerlens.backend.dto.JobRequest;
import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    public JobResponse createJob(String recruiterId, JobRequest request) {
        Optional<Company> companyOpt = companyRepository.findByUserId(recruiterId);
        String companyId = companyOpt.map(Company::getId).orElse(null);

        Job job = new Job(
                null,
                recruiterId,
                companyId,
                request.getTitle(),
                request.getDescription(),
                request.getRequiredSkills() != null ? request.getRequiredSkills() : List.of(),
                request.getPreferredSkills() != null ? request.getPreferredSkills() : List.of(),
                request.getMinimumExperience(),
                request.getMaximumExperience(),
                request.getEducation(),
                request.getSalaryMin(),
                request.getSalaryMax(),
                request.getLocation(),
                request.getWorkMode(),
                request.getJobType(),
                request.getResponsibilities() != null ? request.getResponsibilities() : List.of(),
                request.getRequirements() != null ? request.getRequirements() : List.of(),
                request.getActive() != null ? request.getActive() : true,
                Instant.now(),
                Instant.now()
        );

        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    public Page<JobResponse> getRecruiterJobs(String recruiterId, Pageable pageable) {
        return jobRepository.findByRecruiterId(recruiterId, pageable)
                .map(this::mapToResponse);
    }

    public JobResponse getRecruiterJobById(String id, String recruiterId) {
        Job job = jobRepository.findByIdAndRecruiterId(id, recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied."));
        return mapToResponse(job);
    }

    public JobResponse updateJob(String id, String recruiterId, JobRequest request) {
        Job job = jobRepository.findByIdAndRecruiterId(id, recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied."));

        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequiredSkills() != null) job.setRequiredSkills(request.getRequiredSkills());
        if (request.getPreferredSkills() != null) job.setPreferredSkills(request.getPreferredSkills());
        if (request.getMinimumExperience() != null) job.setMinimumExperience(request.getMinimumExperience());
        if (request.getMaximumExperience() != null) job.setMaximumExperience(request.getMaximumExperience());
        if (request.getEducation() != null) job.setEducation(request.getEducation());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getWorkMode() != null) job.setWorkMode(request.getWorkMode());
        if (request.getJobType() != null) job.setJobType(request.getJobType());
        if (request.getResponsibilities() != null) job.setResponsibilities(request.getResponsibilities());
        if (request.getRequirements() != null) job.setRequirements(request.getRequirements());
        if (request.getActive() != null) job.setActive(request.getActive());

        job.setUpdatedAt(Instant.now());
        Job saved = jobRepository.save(job);
        return mapToResponse(saved);
    }

    public void deleteJob(String id, String recruiterId) {
        Job job = jobRepository.findByIdAndRecruiterId(id, recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or access denied."));
        jobRepository.delete(job);
    }

    public Page<JobResponse> searchJobs(String keyword, List<String> skills, String location,
                                         WorkMode workMode, JobType jobType, Integer minExperience,
                                         Double minSalary, String companyId, Pageable pageable) {
        return jobRepository.searchJobs(keyword, skills, location, workMode, jobType,
                        minExperience, minSalary, companyId, pageable)
                .map(this::mapToResponse);
    }

    public JobResponse getPublicJobById(String id) {
        Job job = jobRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or is no longer active."));
        return mapToResponse(job);
    }

    public Job getJobEntityById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    public JobResponse mapToResponse(Job job) {
        String companyName = "CareerLens Partner";
        String companyLogo = null;
        if (job.getCompanyId() != null) {
            Optional<Company> companyOpt = companyRepository.findById(job.getCompanyId());
            if (companyOpt.isPresent()) {
                companyName = companyOpt.get().getName();
                companyLogo = companyOpt.get().getLogo();
            }
        } else if (job.getRecruiterId() != null) {
            Optional<Company> companyOpt = companyRepository.findByUserId(job.getRecruiterId());
            if (companyOpt.isPresent()) {
                companyName = companyOpt.get().getName();
                companyLogo = companyOpt.get().getLogo();
            }
        }
        return JobResponse.fromEntity(job, companyName, companyLogo);
    }
}
