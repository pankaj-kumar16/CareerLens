package com.careerlens.backend.service;

import com.careerlens.backend.dto.AuthResponse;
import com.careerlens.backend.dto.LoginSendOtpRequest;
import com.careerlens.backend.dto.LoginVerifyOtpRequest;
import com.careerlens.backend.dto.RegisterSendOtpRequest;
import com.careerlens.backend.dto.RegisterVerifyOtpRequest;
import com.careerlens.backend.model.JobSeekerProfile;
import com.careerlens.backend.model.OtpType;
import com.careerlens.backend.model.Role;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.JobSeekerProfileRepository;
import com.careerlens.backend.repository.RecruiterProfileRepository;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @MockBean
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Registration - Duplicate Email Prevention")
    void testRegisterSendOtp_DuplicateEmail() {
        RegisterSendOtpRequest req = new RegisterSendOtpRequest();
        req.setEmail("existing@example.com");
        req.setRole(Role.JOB_SEEKER);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.registerSendOtp(req));
    }

    @Test
    @DisplayName("Registration - Verify OTP Success & User/Profile Creation")
    void testRegisterVerifyOtp_Success() {
        RegisterVerifyOtpRequest req = new RegisterVerifyOtpRequest();
        req.setEmail("newuser@example.com");
        req.setOtp("123456");
        req.setRole(Role.JOB_SEEKER);
        req.setFullName("John Seeker");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(otpService.verifyOtp("newuser@example.com", "123456", OtpType.REGISTER)).thenReturn(true);

        User savedUser = new User("user123", "newuser@example.com", "John Seeker", null, Role.JOB_SEEKER, true, true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn("mockJwtToken");

        AuthResponse res = authService.registerVerifyOtp(req);

        assertNotNull(res);
        assertEquals("mockJwtToken", res.getToken());
        assertEquals("newuser@example.com", res.getUser().getEmail());
        assertTrue(res.getUser().isEmailVerified());
        verify(jobSeekerProfileRepository, times(1)).save(any(JobSeekerProfile.class));
    }

    @Test
    @DisplayName("Login - Unregistered User Prevention")
    void testLoginSendOtp_UnregisteredUser() {
        LoginSendOtpRequest req = new LoginSendOtpRequest();
        req.setEmail("unregistered@example.com");

        when(userRepository.findByEmail("unregistered@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.loginSendOtp(req));
    }

    @Test
    @DisplayName("Login - Verify OTP Success & JWT Issuance")
    void testLoginVerifyOtp_Success() {
        LoginVerifyOtpRequest req = new LoginVerifyOtpRequest();
        req.setEmail("user@example.com");
        req.setOtp("654321");

        User user = new User("user99", "user@example.com", "Jane Recruiter", null, Role.RECRUITER, true, true);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOtp("user@example.com", "654321", OtpType.LOGIN)).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com", "RECRUITER", "user99")).thenReturn("mockRecruiterJwt");

        AuthResponse res = authService.loginVerifyOtp(req);

        assertNotNull(res);
        assertEquals("mockRecruiterJwt", res.getToken());
        assertEquals(Role.RECRUITER, res.getUser().getRole());
    }
}
