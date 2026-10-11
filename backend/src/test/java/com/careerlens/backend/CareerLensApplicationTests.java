package com.careerlens.backend;

import com.careerlens.backend.model.OtpRecord;
import com.careerlens.backend.model.OtpType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017/careerlens_test",
    "spring.data.mongodb.database=careerlens_test",
    "jwt.secret=test-secret-key-for-unit-tests-only-not-for-production-which-is-at-least-sixty-four-bytes-long",
    "jwt.expiration-ms=3600000",
    "otp.expiry-minutes=5",
    "otp.max-attempts=3",
    "cors.allowed-origins=http://localhost:5173,http://localhost:8080"
})
class CareerLensApplicationTests {

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void healthEndpointReturnsUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/health", Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    void mongoConnectionWorks() {
        OtpRecord record = new OtpRecord(
                uniqueEmail("mongo"),
                "hash123",
                OtpType.REGISTER,
                false,
                0,
                Instant.now().plusSeconds(300)
        );

        OtpRecord saved = mongoTemplate.save(record);
        assertNotNull(saved.getId());

        OtpRecord found = mongoTemplate.findById(saved.getId(), OtpRecord.class);
        assertNotNull(found);
        assertEquals(saved.getEmail(), found.getEmail());
    }
}
