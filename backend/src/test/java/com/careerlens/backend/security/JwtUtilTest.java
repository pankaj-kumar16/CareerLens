package com.careerlens.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            "test-secret-key-for-unit-tests-only-not-for-production-which-is-at-least-sixty-four-bytes-long",
            3600000
    );

    @Test
    void generateToken_shouldCreateValidToken() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER", "user123");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER", "user123");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        JwtUtil shortLived = new JwtUtil(
                "test-secret-key-for-unit-tests-only-not-for-production-which-is-at-least-sixty-four-bytes-long",
                -1000
        );
        String token = shortLived.generateToken("test@example.com", "JOB_SEEKER", "user123");
        assertFalse(shortLived.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void getEmailFromToken_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER", "user123");
        assertEquals("test@example.com", jwtUtil.getEmailFromToken(token));
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtUtil.generateToken("test@example.com", "RECRUITER", "user123");
        assertEquals("RECRUITER", jwtUtil.getRoleFromToken(token));
    }
}
