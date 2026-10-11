package com.careerlens.backend.controller;

import com.careerlens.backend.dto.ApplicationResponse;
import com.careerlens.backend.dto.JobResponse;
import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.User;
import com.careerlens.backend.model.WorkMode;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.service.ApplicationService;
import com.careerlens.backend.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class PublicJobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public PublicJobController(JobService jobService,
                               ApplicationService applicationService,
                               UserRepository userRepository) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) String companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobResponse> jobs = jobService.searchJobs(keyword, skills, location, workMode, jobType,
                minExperience, minSalary, companyId, pageable);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable String id) {
        JobResponse job = jobService.getPublicJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{jobId}/apply")
    public ResponseEntity<ApplicationResponse> applyForJob(Authentication authentication,
                                                           @PathVariable String jobId,
                                                           @RequestParam(required = false) String resumeId) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        ApplicationResponse response = applicationService.applyForJob(user.getId(), jobId, resumeId);
        return ResponseEntity.ok(response);
    }
}
