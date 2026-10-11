package com.careerlens.backend.service;

import com.careerlens.backend.dto.ResumeResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.ParsingStatus;
import com.careerlens.backend.model.Resume;
import com.careerlens.backend.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        FileStorageService storageService = new LocalFileStorageService("./target/test-uploads");
        resumeService = new ResumeService(resumeRepository, storageService);
    }

    @Test
    void testUploadResumeSuccess() {
        String jobSeekerId = "user123";
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample_resume.pdf", "application/pdf", "%PDF-1.4 Mock PDF Content".getBytes()
        );

        when(resumeRepository.countByJobSeekerId(jobSeekerId)).thenReturn(0L);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
            Resume r = invocation.getArgument(0);
            r.setId("resume123");
            return r;
        });

        ResumeResponse response = resumeService.uploadResume(jobSeekerId, file);

        assertNotNull(response);
        assertEquals("sample_resume.pdf", response.getOriginalFileName());
        assertEquals("user123", response.getJobSeekerId());
        verify(resumeRepository, times(1)).save(any(Resume.class));
    }

    @Test
    void testUnsupportedResumeFormatIsRejected() {
        String jobSeekerId = "user123";
        MockMultipartFile file = new MockMultipartFile(
                "file", "virus_script.exe", "application/x-msdownload", "executable data".getBytes()
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                resumeService.uploadResume(jobSeekerId, file)
        );

        assertTrue(ex.getMessage().contains("Only PDF and DOCX files are supported"));
        verify(resumeRepository, never()).save(any(Resume.class));
    }

    @Test
    void testUploadFirstResumeBecomesDefault() {
        String jobSeekerId = "user123";
        MockMultipartFile file = new MockMultipartFile(
                "file", "backend_resume.pdf", "application/pdf", "%PDF-1.4 Mock PDF Content".getBytes()
        );

        when(resumeRepository.countByJobSeekerId(jobSeekerId)).thenReturn(0L);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(i -> {
            Resume r = i.getArgument(0);
            r.setId("res1");
            return r;
        });

        ResumeResponse response = resumeService.uploadResumeVersion(jobSeekerId, "Backend Resume", file);

        assertNotNull(response);
        assertEquals("Backend Resume", response.getTitle());
        assertTrue(response.isDefault());
    }

    @Test
    void testUploadSecondResumeIsNotDefault() {
        String jobSeekerId = "user123";
        MockMultipartFile file = new MockMultipartFile(
                "file", "fullstack_resume.pdf", "application/pdf", "%PDF-1.4 Mock PDF Content".getBytes()
        );

        when(resumeRepository.countByJobSeekerId(jobSeekerId)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(i -> {
            Resume r = i.getArgument(0);
            r.setId("res2");
            return r;
        });

        ResumeResponse response = resumeService.uploadResumeVersion(jobSeekerId, "Full Stack Resume", file);

        assertNotNull(response);
        assertEquals("Full Stack Resume", response.getTitle());
        assertFalse(response.isDefault());
    }

    @Test
    void testSetDefaultResumeUpdatesPreviousDefault() {
        String jobSeekerId = "user123";
        Resume r1 = new Resume();
        r1.setId("res1");
        r1.setJobSeekerId(jobSeekerId);
        r1.setDefault(true);

        Resume r2 = new Resume();
        r2.setId("res2");
        r2.setJobSeekerId(jobSeekerId);
        r2.setDefault(false);

        when(resumeRepository.findByIdAndJobSeekerId("res2", jobSeekerId)).thenReturn(Optional.of(r2));
        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId)).thenReturn(java.util.List.of(r1, r2));

        ResumeResponse response = resumeService.setDefaultResume("res2", jobSeekerId);

        assertTrue(response.isDefault());
        assertFalse(r1.isDefault());
    }

    @Test
    void testUserCannotAccessMissingResume() {
        String jobSeekerId = "user999";
        when(resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)).thenReturn(Optional.empty());
        when(resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId)).thenReturn(java.util.Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () ->
                resumeService.getResumeByJobSeekerId(jobSeekerId)
        );
    }
}
