package com.careerlens.backend.service;

import com.careerlens.backend.model.OtpRecord;
import com.careerlens.backend.model.OtpType;
import com.careerlens.backend.repository.OtpRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class OtpServiceTest {

    @MockBean
    private OtpRecordRepository otpRecordRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 5);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 3);
        ReflectionTestUtils.setField(otpService, "resendCooldownSeconds", 60);
        ReflectionTestUtils.setField(otpService, "rateLimitPerHour", 5);
    }

    @Test
    @DisplayName("OTP Generation & Delivery")
    void testGenerateAndSendOtp_Success() {
        when(otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(otpRecordRepository.countByEmailAndTypeAndCreatedAtAfterAndUsedFalse(anyString(), any(), any()))
                .thenReturn(0);

        otpService.generateAndSendOtp("test@example.com", OtpType.REGISTER);

        verify(emailService, times(1)).sendOtpEmail(eq("test@example.com"), anyString());
        verify(otpRecordRepository, times(1)).save(any(OtpRecord.class));
    }

    @Test
    @DisplayName("OTP Resend Cooldown Violation")
    void testGenerateAndSendOtp_CooldownActive() {
        OtpRecord activeRecord = new OtpRecord("test@example.com", "hash", OtpType.REGISTER, false, 0, Instant.now().plus(5, ChronoUnit.MINUTES));
        activeRecord.setCreatedAt(Instant.now().minus(30, ChronoUnit.SECONDS));

        when(otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(activeRecord));

        assertThrows(IllegalStateException.class, () ->
                otpService.generateAndSendOtp("test@example.com", OtpType.REGISTER)
        );
    }

    @Test
    @DisplayName("OTP Rate Limit Per Hour Violation")
    void testGenerateAndSendOtp_RateLimitExceeded() {
        when(otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        when(otpRecordRepository.countByEmailAndTypeAndCreatedAtAfterAndUsedFalse(anyString(), any(), any()))
                .thenReturn(5);

        assertThrows(IllegalStateException.class, () ->
                otpService.generateAndSendOtp("test@example.com", OtpType.REGISTER)
        );
    }

    @Test
    @DisplayName("OTP Expiration Rejection")
    void testVerifyOtp_Expired() {
        OtpRecord expiredRecord = new OtpRecord("test@example.com", "dummyhash", OtpType.REGISTER, false, 0, Instant.now().minus(1, ChronoUnit.MINUTES));

        when(otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(expiredRecord));

        boolean result = otpService.verifyOtp("test@example.com", "123456", OtpType.REGISTER);
        assertFalse(result);
    }

    @Test
    @DisplayName("Invalid OTP & Attempt Limit Enforcement")
    void testVerifyOtp_AttemptLimitReached() {
        OtpRecord record = new OtpRecord("test@example.com", "nonmatchinghash", OtpType.REGISTER, false, 2, Instant.now().plus(5, ChronoUnit.MINUTES));

        when(otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(record));

        boolean result = otpService.verifyOtp("test@example.com", "000000", OtpType.REGISTER);

        assertFalse(result);
        assertEquals(3, record.getAttemptCount());
        assertTrue(record.isUsed());
        verify(otpRecordRepository, times(1)).save(record);
    }

    @Test
    @DisplayName("Already Used OTP Rejection")
    void testVerifyOtp_UsedRecordNotReturned() {
        when(otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());

        boolean result = otpService.verifyOtp("test@example.com", "123456", OtpType.REGISTER);
        assertFalse(result);
    }
}
