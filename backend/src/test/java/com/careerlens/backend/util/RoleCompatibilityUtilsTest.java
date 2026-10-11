package com.careerlens.backend.util;

import com.careerlens.backend.model.Job;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RoleCompatibilityUtilsTest {

    private Job createJob(String title, boolean active) {
        Job job = new Job();
        job.setTitle(title);
        job.setActive(active);
        return job;
    }

    @Test
    @DisplayName("Test A: Full Stack Developer resume matches 'full stack developer' job title")
    void testA_FullStackMatchesLowercase() {
        Set<String> roles = Set.of("Full Stack Developer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("full stack developer", true), roles));
    }

    @Test
    @DisplayName("Test B: Full Stack Developer resume matches 'Full-Stack Developer' job title")
    void testB_FullStackMatchesHyphenated() {
        Set<String> roles = Set.of("Full Stack Developer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Full-Stack Developer", true), roles));
    }

    @Test
    @DisplayName("Test C: Full Stack Developer resume matches 'Backend Developer' job title")
    void testC_FullStackMatchesBackend() {
        Set<String> roles = Set.of("Full Stack Developer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Backend Developer", true), roles));
    }

    @Test
    @DisplayName("Test D: Full Stack Developer resume DOES NOT match 'Data Analyst' job title")
    void testD_FullStackExcludesDataAnalyst() {
        Set<String> roles = Set.of("Full Stack Developer");
        assertFalse(RoleCompatibilityUtils.isCompatible(createJob("Data Analyst", true), roles));
    }

    @Test
    @DisplayName("Test E: Backend Developer resume DOES NOT match 'DevOps Engineer' job title")
    void testE_BackendExcludesDevOps() {
        Set<String> roles = Set.of("Backend Developer");
        assertFalse(RoleCompatibilityUtils.isCompatible(createJob("DevOps Engineer", true), roles));
    }

    @Test
    @DisplayName("Test F: Multiple resumes (Backend Developer & Python Developer) match Backend and Python, exclude Data Analyst")
    void testF_MultiResumeFiltering() {
        Set<String> roles = Set.of("Backend Developer", "Python Developer");

        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Backend Developer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Python Developer", true), roles));
        assertFalse(RoleCompatibilityUtils.isCompatible(createJob("Data Analyst", true), roles));
    }
}
