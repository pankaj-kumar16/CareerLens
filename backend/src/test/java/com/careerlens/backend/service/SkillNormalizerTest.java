package com.careerlens.backend.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkillNormalizerTest {

    @Test
    void testNormalizeSingleSkills() {
        assertEquals("JavaScript", SkillNormalizer.normalize("JS"));
        assertEquals("JavaScript", SkillNormalizer.normalize("javascript"));
        assertEquals("React", SkillNormalizer.normalize("ReactJS"));
        assertEquals("React", SkillNormalizer.normalize("react.js"));
        assertEquals("Node.js", SkillNormalizer.normalize("NodeJS"));
        assertEquals("MongoDB", SkillNormalizer.normalize("Mongo DB"));
        assertEquals("Spring Boot", SkillNormalizer.normalize("SpringBoot"));
        assertEquals("TypeScript", SkillNormalizer.normalize("TS"));
        assertEquals("AWS", SkillNormalizer.normalize("Amazon Web Services"));
    }

    @Test
    void testNormalizeListDeduplication() {
        List<String> raw = Arrays.asList("JS", "JavaScript", "reactjs", "React", "Mongo DB");
        List<String> normalized = SkillNormalizer.normalizeList(raw);

        assertEquals(3, normalized.size());
        assertTrue(normalized.contains("JavaScript"));
        assertTrue(normalized.contains("React"));
        assertTrue(normalized.contains("MongoDB"));
    }
}
