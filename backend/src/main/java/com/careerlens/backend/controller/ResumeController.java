package com.careerlens.backend.controller;

import com.careerlens.backend.dto.ApiResponse;
import com.careerlens.backend.dto.ResumeResponse;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.service.ResumeService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/job-seeker")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepository userRepository;

    public ResumeController(ResumeService resumeService, UserRepository userRepository) {
        this.resumeService = resumeService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    // --- Single Resume (Default Resume Wrapper for backward compatibility) ---

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(Authentication authentication,
                                                       @RequestParam(value = "title", required = false) String title,
                                                       @RequestParam("file") MultipartFile file) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse response = resumeService.uploadResumeVersion(user.getId(), title, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeResponse> getResume(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse response = resumeService.getResumeByJobSeekerId(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resume/download")
    public ResponseEntity<Resource> downloadResume(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse metadata = resumeService.getResumeByJobSeekerId(user.getId());
        Resource resource = resumeService.loadResumeFile(user.getId());

        String contentType = metadata.getFileType() != null ? metadata.getFileType() : "application/octet-stream";
        String originalFileName = metadata.getOriginalFileName() != null ? metadata.getOriginalFileName() : "resume.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/resume")
    public ResponseEntity<ApiResponse<String>> deleteResume(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        resumeService.deleteResume(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Resumes deleted successfully."));
    }

    // --- Multi-Resume Version Endpoints ---

    @GetMapping("/resumes")
    public ResponseEntity<List<ResumeResponse>> getAllResumes(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<ResumeResponse> response = resumeService.getAllResumesForJobSeeker(user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/resumes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResumeVersion(Authentication authentication,
                                                              @RequestParam(value = "title", required = false) String title,
                                                              @RequestParam("file") MultipartFile file) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse response = resumeService.uploadResumeVersion(user.getId(), title, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resumes/{id}")
    public ResponseEntity<ResumeResponse> getResumeVersionById(Authentication authentication,
                                                               @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse response = resumeService.getResumeVersionById(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/resumes/{id}/download")
    public ResponseEntity<Resource> downloadResumeVersion(Authentication authentication,
                                                          @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse metadata = resumeService.getResumeVersionById(id, user.getId());
        Resource resource = resumeService.loadResumeFileByIdAndJobSeekerId(id, user.getId());

        String contentType = metadata.getFileType() != null ? metadata.getFileType() : "application/octet-stream";
        String originalFileName = metadata.getOriginalFileName() != null ? metadata.getOriginalFileName() : "resume.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                .body(resource);
    }

    @PutMapping("/resumes/{id}/default")
    public ResponseEntity<ResumeResponse> setDefaultResume(Authentication authentication,
                                                           @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse response = resumeService.setDefaultResume(id, user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/resumes/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> updateResumeVersion(Authentication authentication,
                                                              @PathVariable String id,
                                                              @RequestParam(value = "title", required = false) String title,
                                                              @RequestParam(value = "file", required = false) MultipartFile file) {
        User user = getAuthenticatedUser(authentication);
        ResumeResponse response = resumeService.updateResumeVersion(id, user.getId(), title, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/resumes/{id}")
    public ResponseEntity<ApiResponse<String>> deleteResumeVersion(Authentication authentication,
                                                                    @PathVariable String id) {
        User user = getAuthenticatedUser(authentication);
        resumeService.deleteResumeVersion(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Resume version deleted successfully."));
    }
}
