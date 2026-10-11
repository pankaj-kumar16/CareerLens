package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "otp_records")
public class OtpRecord {

    @Id
    private String id;

    private String email;
    private String otpHash;
    private OtpType type;
    private boolean used;
    private int attemptCount;
    private Instant expiresAt;
    private Instant createdAt = Instant.now();

    public OtpRecord() {}

    public OtpRecord(String email, String otpHash, OtpType type, boolean used, int attemptCount, Instant expiresAt) {
        this.email = email;
        this.otpHash = otpHash;
        this.type = type;
        this.used = used;
        this.attemptCount = attemptCount;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

    public OtpType getType() { return type; }
    public void setType(OtpType type) { this.type = type; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
