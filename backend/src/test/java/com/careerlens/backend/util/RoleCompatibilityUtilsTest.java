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

    @Test
    @DisplayName("Test G: Full Stack Developer matches Full Stack Engineer and MERN Stack Developer")
    void testG_FullStackSynonyms() {
        Set<String> roles = Set.of("full stack developer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Full Stack Engineer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("MERN Stack Developer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Fullstack Software Engineer", true), roles));
    }

    @Test
    @DisplayName("Test H: Technical skill overlap matches when candidate has required skills")
    void testH_SkillOverlapCompatibility() {
        Job job = createJob("Senior Systems Engineer", true);
        job.setRequiredSkills(java.util.List.of("React", "Node.js", "MongoDB"));

        Set<String> roles = Set.of("software developer");
        java.util.List<String> candidateSkills = java.util.List.of("React", "Express", "MongoDB");

        assertTrue(RoleCompatibilityUtils.isCompatible(job, roles, candidateSkills));
    }

    @Test
    @DisplayName("Test I: React Developer resume matches React.js, React Engineer, and Frontend Developer")
    void testI_ReactDeveloperCompatibility() {
        Set<String> roles = Set.of("react developer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("React Developer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("React.js Developer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Senior React Engineer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Frontend Developer", true), roles));
    }

    @Test
    @DisplayName("Test J: Python Developer resume matches Python Backend, Django, and FastAPI jobs")
    void testJ_PythonDeveloperCompatibility() {
        Set<String> roles = Set.of("python developer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Python Developer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Python Backend Engineer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Django Developer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("FastAPI Backend Engineer", true), roles));
    }

    @Test
    @DisplayName("Test K: DevOps Engineer resume matches job with leading space and unicode en-dash")
    void testK_DevOpsUnicodeDashCompatibility() {
        Set<String> roles = Set.of("devops engineer");
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob(" DevOps Engineer", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob(" DevOps Engineer – Cloud Infrastructure & Automation", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Site Reliability Engineer (SRE)", true), roles));
        assertTrue(RoleCompatibilityUtils.isCompatible(createJob("Cloud Infrastructure Engineer", true), roles));
    }
}
