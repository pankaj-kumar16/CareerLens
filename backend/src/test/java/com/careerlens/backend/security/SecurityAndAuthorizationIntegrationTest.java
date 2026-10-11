package com.careerlens.backend.security;

import com.careerlens.backend.model.Role;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    private String jobSeekerToken;
    private String recruiterToken;

    @BeforeEach
    void setUp() {
        User seeker = new User("seeker1", "seeker@example.com", "Job Seeker", null, Role.JOB_SEEKER, true, true);
        User recruiter = new User("rec1", "recruiter@example.com", "Recruiter", null, Role.RECRUITER, true, true);

        when(userRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(seeker));
        when(userRepository.findByEmail("recruiter@example.com")).thenReturn(Optional.of(recruiter));

        jobSeekerToken = jwtUtil.generateToken("seeker@example.com", "JOB_SEEKER", "seeker1");
        recruiterToken = jwtUtil.generateToken("recruiter@example.com", "RECRUITER", "rec1");
    }

    @Test
    @DisplayName("Public Health Endpoint - Accessible Without Auth")
    void testPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected Job Seeker Endpoint - Rejected Without Token")
    void testJobSeekerEndpoint_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/job-seeker/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected Job Seeker Endpoint - Allowed for JOB_SEEKER Token")
    void testJobSeekerEndpoint_Authorized() throws Exception {
        mockMvc.perform(get("/api/job-seeker/profile")
                        .header("Authorization", "Bearer " + jobSeekerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected Job Seeker Endpoint - Forbidden for RECRUITER Token")
    void testJobSeekerEndpoint_ForbiddenForRecruiter() throws Exception {
        mockMvc.perform(get("/api/job-seeker/profile")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected Recruiter Endpoint - Allowed for RECRUITER Token")
    void testRecruiterEndpoint_Authorized() throws Exception {
        mockMvc.perform(get("/api/recruiter/profile")
                        .header("Authorization", "Bearer " + recruiterToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Protected Recruiter Endpoint - Forbidden for JOB_SEEKER Token")
    void testRecruiterEndpoint_ForbiddenForJobSeeker() throws Exception {
        mockMvc.perform(get("/api/recruiter/profile")
                        .header("Authorization", "Bearer " + jobSeekerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Invalid JWT Token - Rejected")
    void testInvalidJwtToken() throws Exception {
        mockMvc.perform(get("/api/job-seeker/profile")
                        .header("Authorization", "Bearer invalid.jwt.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
