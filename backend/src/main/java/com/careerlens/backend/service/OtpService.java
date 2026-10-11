package com.careerlens.backend.service;

import com.careerlens.backend.model.OtpRecord;
import com.careerlens.backend.model.OtpType;
import com.careerlens.backend.repository.OtpRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final OtpRecordRepository otpRecordRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Value("${otp.rate-limit-per-hour:5}")
    private int rateLimitPerHour;

    public OtpService(OtpRecordRepository otpRecordRepository, EmailService emailService) {
        this.otpRecordRepository = otpRecordRepository;
        this.emailService = emailService;
    }

    public void generateAndSendOtp(String email, OtpType type) {
        String code = String.format("%06d", random.nextInt(900000) + 100000);
        String otpHash = hashOtp(code);

        Instant now = Instant.now();
        Instant cooldownThreshold = now.minus(resendCooldownSeconds, ChronoUnit.SECONDS);

        Optional<OtpRecord> recentRecord = otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type);
        if (recentRecord.isPresent() && recentRecord.get().getCreatedAt().isAfter(cooldownThreshold)) {
            throw new IllegalStateException("Please wait before requesting a new OTP.");
        }

        int recentCount = otpRecordRepository.countByEmailAndTypeAndCreatedAtAfterAndUsedFalse(
                email, type, now.minus(1, ChronoUnit.HOURS));
        if (recentCount >= rateLimitPerHour) {
            throw new IllegalStateException("Too many OTP requests. Please try again later.");
        }

        otpRecordRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type)
                .ifPresent(record -> {
                    record.setUsed(true);
                    otpRecordRepository.save(record);
                });

        OtpRecord record = new OtpRecord(
                email,
                otpHash,
                type,
                false,
                0,
                now.plus(expiryMinutes, ChronoUnit.MINUTES)
        );

        otpRecordRepository.save(record);

        emailService.sendOtpEmail(email, code);
    }

    public boolean verifyOtp(String email, String code, OtpType type) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String otpHash = hashOtp(code.trim());

        Optional<OtpRecord> recordOpt = otpRecordRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type);

        if (recordOpt.isEmpty()) {
            return false;
        }

        OtpRecord record = recordOpt.get();

        if (record.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        if (record.getAttemptCount() >= maxAttempts) {
            record.setUsed(true);
            otpRecordRepository.save(record);
            return false;
        }

        if (!otpHash.equals(record.getOtpHash())) {
            int newAttempts = record.getAttemptCount() + 1;
            record.setAttemptCount(newAttempts);
            if (newAttempts >= maxAttempts) {
                record.setUsed(true);
            }
            otpRecordRepository.save(record);
            return false;
        }

        record.setUsed(true);
        otpRecordRepository.save(record);
        return true;
    }

    private String hashOtp(String code) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
