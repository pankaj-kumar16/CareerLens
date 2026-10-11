package com.careerlens.backend.controller;

import com.careerlens.backend.dto.*;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.service.ApplicationService;
import com.careerlens.backend.service.JobService;
import com.careerlens.backend.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterJobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final ResumeService resumeService;
    private final UserRepository userRepository;

    public RecruiterJobController(JobService jobService,
                                  ApplicationService applicationService,
                                  ResumeService resumeService,
                                  UserRepository userRepository) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.resumeService = resumeService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated recruiter not found."));
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobResponse> createJob(Authentication authentication,
                                                 @Valid @RequestBody JobRequest request) {
        User user = getAuthenticatedUser(authentication);
        JobResponse response = jobService.createJob(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobResponse>> getRecruiterJobs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = getAuthenticatedUser(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.getRecruiterJobs(user.getId(), pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> getRecruiterJobById(Authentication authentication,
                                                           @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        JobResponse response = jobService.getRecruiterJobById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobResponse> updateJob(Authentication authentication,
                                                 @PathVariable String id,
                                                 @Valid @RequestBody JobRequest request) {
        User user = getAuthenticatedUser(authentication);
        JobResponse response = jobService.updateJob(id, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<String>> deleteJob(Authentication authentication,
                                                          @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        jobService.deleteJob(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Job deleted successfully."));
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getJobApplications(Authentication authentication,
                                                                        @PathVariable String jobId) {
        User user = getAuthenticatedUser(authentication);
        List<ApplicationResponse> applications = applicationService.getApplicationsForRecruiterJob(jobId, user.getId());
        return ResponseEntity.ok(applications);
    }

    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            Authentication authentication,
            @PathVariable String applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        User user = getAuthenticatedUser(authentication);
        ApplicationResponse response = applicationService.updateApplicationStatus(
                applicationId, user.getId(), request.getStatus(), request.getInterviewDate()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/applications/{applicationId}/notes")
    public ResponseEntity<ApplicationResponse> updateApplicationNotes(
            Authentication authentication,
            @PathVariable String applicationId,
            @RequestBody ApplicationNotesUpdateRequest request
    ) {
        User user = getAuthenticatedUser(authentication);
        ApplicationResponse response = applicationService.updateApplicationNotes(
                applicationId, user.getId(), request.getRecruiterNotes()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shortlisted")
    public ResponseEntity<List<ApplicationResponse>> getShortlistedCandidates(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<ApplicationResponse> shortlisted = applicationService.getShortlistedCandidatesForRecruiter(user.getId());
        return ResponseEntity.ok(shortlisted);
    }

    @GetMapping("/applications/{applicationId}/resume/download")
    public ResponseEntity<Resource> downloadCandidateResume(Authentication authentication,
                                                            @PathVariable String applicationId) {
        User user = getAuthenticatedUser(authentication);
        ApplicationResponse app = applicationService.updateApplicationNotes(applicationId, user.getId(), null);

        Resource resource = null;
        if (StringUtils.hasText(app.getResumeId())) {
            try {
                resource = resumeService.loadResumeFileById(app.getResumeId());
            } catch (Exception ignored) {}
        }

        if (resource == null && StringUtils.hasText(app.getJobSeekerId())) {
            try {
                resource = resumeService.loadResumeFile(app.getJobSeekerId());
            } catch (Exception ignored) {}
        }

        if (resource == null) {
            throw new ResourceNotFoundException("Resume file not found for candidate.");
        }

        String fileName = StringUtils.hasText(app.getResumeOriginalFileName()) ? app.getResumeOriginalFileName() : "candidate_resume.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
