package com.careerlens.backend.controller;

import com.careerlens.backend.dto.*;
import com.careerlens.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<String>> registerSendOtp(@Valid @RequestBody RegisterSendOtpRequest request) {
        authService.registerSendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to email. Please verify to complete registration.", null));
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> registerVerifyOtp(@Valid @RequestBody RegisterVerifyOtpRequest request) {
        AuthResponse response = authService.registerVerifyOtp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful.", response));
    }

    @PostMapping("/login/send-otp")
    public ResponseEntity<ApiResponse<String>> loginSendOtp(@Valid @RequestBody LoginSendOtpRequest request) {
        authService.loginSendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to email. Please verify to login.", null));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> loginVerifyOtp(@Valid @RequestBody LoginVerifyOtpRequest request) {
        AuthResponse response = authService.loginVerifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP resent successfully.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthenticated user"));
        }
        UserResponse user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved.", user));
    }
}
