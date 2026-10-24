package com.careerlens.backend.util;

import com.careerlens.backend.model.Resume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RoleDetectorUtilsTest {

    @Test
    @DisplayName("Detects Full Stack Developer from title/headline")
    void testDetectFullStackFromTitle() {
        Resume resume = new Resume();
        resume.setTitle("Full Stack Developer");
        resume.setSkills(List.of("React", "Node.js", "MongoDB"));

        String role = RoleDetectorUtils.detectRole(resume);
        assertEquals("Full Stack Developer", role);

        Set<String> allRoles = RoleDetectorUtils.detectAllRoles(resume);
        assertTrue(allRoles.contains("full stack developer"));
    }

    @Test
    @DisplayName("Detects Full Stack Developer from dual frontend + backend skills")
    void testDetectFullStackFromDualSkills() {
        Resume resume = new Resume();
        resume.setTitle("Software Engineer");
        resume.setSkills(List.of("React", "CSS", "Node.js", "Express", "MongoDB"));

        String role = RoleDetectorUtils.detectRole(resume);
        assertEquals("Full Stack Developer", role);

        Set<String> allRoles = RoleDetectorUtils.detectAllRoles(resume);
        assertTrue(allRoles.contains("full stack developer"));
        assertTrue(allRoles.contains("frontend developer"));
        assertTrue(allRoles.contains("backend developer"));
    }

    @Test
    @DisplayName("Detects Full Stack from MERN stack filename or summary")
    void testDetectFullStackFromMernText() {
        Resume resume = new Resume();
        resume.setOriginalFileName("John_Doe_MERN_Stack_Resume.pdf");
        resume.setSummary("Passionate MERN stack developer with 3 years experience");

        String role = RoleDetectorUtils.detectRole(resume);
        assertEquals("Full Stack Developer", role);

        Set<String> allRoles = RoleDetectorUtils.detectAllRoles(resume);
        assertTrue(allRoles.contains("full stack developer"));
    }

    @Test
    @DisplayName("Detects React Developer from title and skills")
    void testDetectReactDeveloper() {
        Resume resume = new Resume();
        resume.setTitle("React Developer");
        resume.setSkills(List.of("React", "JavaScript", "HTML", "CSS", "Tailwind CSS"));

        String role = RoleDetectorUtils.detectRole(resume);
        assertEquals("Frontend Developer", role);

        Set<String> allRoles = RoleDetectorUtils.detectAllRoles(resume);
        assertTrue(allRoles.contains("react developer"));
        assertTrue(allRoles.contains("frontend developer"));
    }

    @Test
    @DisplayName("Detects Python Developer from title and skills")
    void testDetectPythonDeveloper() {
        Resume resume = new Resume();
        resume.setTitle("Python Developer");
        resume.setSkills(List.of("Python", "Django", "PostgreSQL"));

        String role = RoleDetectorUtils.detectRole(resume);
        assertEquals("Python Developer", role);

        Set<String> allRoles = RoleDetectorUtils.detectAllRoles(resume);
        assertTrue(allRoles.contains("python developer"));
    }

    @Test
    @DisplayName("Detects DevOps Engineer from skills (Docker, Kubernetes, AWS, Linux)")
    void testDetectDevOpsFromSkills() {
        Resume resume = new Resume();
        resume.setOriginalFileName("Ashutosh_Pandey_resume.pdf");
        resume.setSkills(List.of("Python", "MySQL", "Docker", "Kubernetes", "AWS", "GCP", "Git", "Maven", "Linux"));

        String role = RoleDetectorUtils.detectRole(resume);
        assertEquals("DevOps Engineer", role);

        Set<String> allRoles = RoleDetectorUtils.detectAllRoles(resume);
        assertTrue(allRoles.contains("devops engineer"));
        assertTrue(allRoles.contains("cloud engineer"));
    }
}
