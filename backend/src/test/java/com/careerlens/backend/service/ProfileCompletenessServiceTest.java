package com.careerlens.backend.service;

import com.careerlens.backend.dto.ProfileCompletenessResponse;
import com.careerlens.backend.model.JobSeekerProfile;
import com.careerlens.backend.model.Role;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.JobSeekerProfileRepository;
import com.careerlens.backend.repository.RecruiterProfileRepository;
import com.careerlens.backend.repository.ResumeRepository;
import com.careerlens.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileCompletenessServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;

    @Mock
    private ResumeRepository resumeRepository;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(userRepository, jobSeekerProfileRepository, recruiterProfileRepository, resumeRepository);
    }

    @Test
    void testEmptyProfileCalculatesLowPercentage() {
        String email = "seeker@example.com";
        User user = new User("u1", email, "John Doe", "+1234567890", Role.JOB_SEEKER, true, true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findByUserId("u1")).thenReturn(Optional.empty());
        when(resumeRepository.countByJobSeekerId("u1")).thenReturn(0L);

        ProfileCompletenessResponse response = profileService.getJobSeekerProfileCompleteness(email);

        assertNotNull(response);
        assertTrue(response.getPercentage() < 50);
        assertFalse(response.getMissingFields().isEmpty());
    }

    @Test
    void testCompleteProfileCalculates100Percent() {
        String email = "seeker@example.com";
        User user = new User("u1", email, "John Doe", "+1234567890", Role.JOB_SEEKER, true, true);
        JobSeekerProfile profile = new JobSeekerProfile(
                "p1", "u1", "John Doe", "+1234567890", "New York, NY",
                "Java, Spring, React", "B.Tech Computer Science", "3 Years Backend Dev",
                "CareerLens Job Portal", "AWS Certified Developer",
                "Backend Engineer", "Remote, NY", "₹12,00,000", "HYBRID", true, null, null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findByUserId("u1")).thenReturn(Optional.of(profile));
        when(resumeRepository.countByJobSeekerId("u1")).thenReturn(1L);

        ProfileCompletenessResponse response = profileService.getJobSeekerProfileCompleteness(email);

        assertNotNull(response);
        assertEquals(100, response.getPercentage());
        assertTrue(response.getMissingFields().isEmpty());
    }

    @Test
    void testWhitespaceAndEmptyStringsDoNotCountAsComplete() {
        String email = "seeker@example.com";
        User user = new User("u1", email, "John Doe", null, Role.JOB_SEEKER, true, true);
        JobSeekerProfile profile = new JobSeekerProfile(
                "p1", "u1", "   ", "   ", "   ",
                "", "  \t  ", "",
                "", "",
                "", "", "", "", false, null, null
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jobSeekerProfileRepository.findByUserId("u1")).thenReturn(Optional.of(profile));
        when(resumeRepository.countByJobSeekerId("u1")).thenReturn(0L);

        ProfileCompletenessResponse response = profileService.getJobSeekerProfileCompleteness(email);

        assertNotNull(response);
        assertEquals(0, response.getPercentage());
    }
}
