package com.careerlens.backend.service;

import com.careerlens.backend.dto.AuthResponse;
import com.careerlens.backend.dto.LoginSendOtpRequest;
import com.careerlens.backend.dto.LoginVerifyOtpRequest;
import com.careerlens.backend.dto.RegisterSendOtpRequest;
import com.careerlens.backend.dto.RegisterVerifyOtpRequest;
import com.careerlens.backend.dto.ResendOtpRequest;
import com.careerlens.backend.dto.UserResponse;
import com.careerlens.backend.model.JobSeekerProfile;
import com.careerlens.backend.model.OtpType;
import com.careerlens.backend.model.RecruiterProfile;
import com.careerlens.backend.model.Role;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.JobSeekerProfileRepository;
import com.careerlens.backend.repository.OtpRecordRepository;
import com.careerlens.backend.repository.RecruiterProfileRepository;
import com.careerlens.backend.repository.UserRepository;
import com.careerlens.backend.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    public AuthService(UserRepository userRepository,
                       OtpService otpService,
                       JwtUtil jwtUtil,
                       JobSeekerProfileRepository jobSeekerProfileRepository,
                       RecruiterProfileRepository recruiterProfileRepository) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    public void registerSendOtp(RegisterSendOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email address is already registered. Please sign in instead.");
        }

        otpService.generateAndSendOtp(email, OtpType.REGISTER);
    }

    public AuthResponse registerVerifyOtp(RegisterVerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        Role role = request.getRole();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email address is already registered. Please sign in instead.");
        }

        boolean isValid = otpService.verifyOtp(email, request.getOtp(), OtpType.REGISTER);
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP code.");
        }

        User user = new User(null, email, request.getFullName(), request.getPhone(), role, true, true);
        User savedUser = userRepository.save(user);

        if (role == Role.JOB_SEEKER) {
            JobSeekerProfile profile = new JobSeekerProfile(
                    null, savedUser.getId(), savedUser.getFullName(), savedUser.getPhone(),
                    null, null, null, null, null, null, null, null, null, null, false, Instant.now(), Instant.now()
            );
            jobSeekerProfileRepository.save(profile);
        } else if (role == Role.RECRUITER) {
            RecruiterProfile profile = new RecruiterProfile(
                    null, savedUser.getId(), savedUser.getFullName(), savedUser.getPhone(), null, null, Instant.now(), Instant.now()
            );
            recruiterProfileRepository.save(profile);
        }

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId());
        return new AuthResponse(token, UserResponse.fromUser(savedUser), "Registration successful!");
    }

    public void loginSendOtp(LoginSendOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email. Please register first."));

        if (request.getRole() != null && user.getRole() != request.getRole()) {
            String registeredAs = user.getRole() == Role.RECRUITER ? "Recruiter" : "Job Seeker";
            throw new IllegalArgumentException("This email is registered as a " + registeredAs + ". Please select " + registeredAs + " to sign in.");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("Account is disabled.");
        }

        otpService.generateAndSendOtp(email, OtpType.LOGIN);
    }

    public AuthResponse loginVerifyOtp(LoginVerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email. Please register first."));

        if (request.getRole() != null && user.getRole() != request.getRole()) {
            String registeredAs = user.getRole() == Role.RECRUITER ? "Recruiter" : "Job Seeker";
            throw new IllegalArgumentException("This email is registered as a " + registeredAs + ". Please select " + registeredAs + " to sign in.");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("Account is disabled.");
        }

        boolean isValid = otpService.verifyOtp(email, request.getOtp(), OtpType.LOGIN);
        if (!isValid) {
            throw new IllegalArgumentException("Invalid or expired OTP code.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        return new AuthResponse(token, UserResponse.fromUser(user), "Login successful!");
    }

    public void resendOtp(ResendOtpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        OtpType type = request.getType();
        if (type == null) {
            type = request.getRole() != null ? OtpType.REGISTER : OtpType.LOGIN;
        }
        otpService.generateAndSendOtp(email, type);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return UserResponse.fromUser(user);
    }
}
