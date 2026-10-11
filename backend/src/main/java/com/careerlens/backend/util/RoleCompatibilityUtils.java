package com.careerlens.backend.util;

import com.careerlens.backend.model.Job;
import org.springframework.util.StringUtils;

import java.util.*;

public class RoleCompatibilityUtils {

    private static final Map<String, Set<String>> COMPATIBILITY_MAP = new LinkedHashMap<>();

    static {
        // Full Stack Developer
        COMPATIBILITY_MAP.put("full stack developer", Set.of(
            "full stack developer", "full-stack developer", "fullstack developer",
            "full stack", "fullstack", "frontend developer", "backend developer",
            "web developer", "software engineer", "software developer"
        ));

        // Backend Developer
        COMPATIBILITY_MAP.put("backend developer", Set.of(
            "backend developer", "back-end developer", "backend engineer",
            "java developer", "java backend developer", "python developer", "python backend developer",
            "node developer", "node.js backend developer", "server-side developer", "api developer",
            "software engineer", "software developer"
        ));

        // Frontend Developer
        COMPATIBILITY_MAP.put("frontend developer", Set.of(
            "frontend developer", "front-end developer", "frontend engineer",
            "react developer", "angular developer", "vue developer", "ui developer",
            "web developer"
        ));

        // DevOps Engineer
        COMPATIBILITY_MAP.put("devops engineer", Set.of(
            "devops engineer", "devops", "devsecops engineer", "devsecops",
            "cloud engineer", "cloud infrastructure engineer", "infrastructure engineer",
            "site reliability engineer", "sre", "automation engineer"
        ));

        // Python Developer
        COMPATIBILITY_MAP.put("python developer", Set.of(
            "python developer", "python backend developer", "python engineer",
            "python software developer", "backend developer", "software engineer"
        ));

        // Data Analyst
        COMPATIBILITY_MAP.put("data analyst", Set.of(
            "data analyst", "bi analyst", "business intelligence analyst",
            "data analytics specialist", "sql analyst", "reporting analyst",
            "business analyst"
        ));

        // AI/ML Engineer
        COMPATIBILITY_MAP.put("ai/ml engineer", Set.of(
            "ai/ml engineer", "ai/ml", "machine learning engineer", "ml engineer",
            "ai engineer", "generative ai engineer", "nlp engineer", "deep learning engineer",
            "data scientist"
        ));

        // Data Scientist
        COMPATIBILITY_MAP.put("data scientist", Set.of(
            "data scientist", "data science", "machine learning engineer", "ml engineer",
            "ai/ml engineer", "ai engineer"
        ));

        // Cloud Engineer
        COMPATIBILITY_MAP.put("cloud engineer", Set.of(
            "cloud engineer", "cloud infrastructure engineer", "aws cloud engineer",
            "azure cloud engineer", "gcp cloud engineer", "devops engineer", "cloud architect"
        ));

        // Cybersecurity Engineer
        COMPATIBILITY_MAP.put("cybersecurity engineer", Set.of(
            "cybersecurity engineer", "cyber security engineer", "information security engineer",
            "security engineer", "security analyst", "penetration tester", "secops engineer"
        ));

        // Software Engineer (generic)
        COMPATIBILITY_MAP.put("software engineer", Set.of(
            "software engineer", "software developer", "sde", "swe", "programmer",
            "full stack developer", "backend developer", "frontend developer"
        ));
    }

    /**
     * Normalize a role or job title string:
     * - Handle capitalization (lowercase)
     * - Normalize variations of full stack / fullstack / full-stack -> "full stack"
     * - Normalize whitespace and non-alphanumeric separators
     */
    public static String normalizeRole(String raw) {
        if (!StringUtils.hasText(raw)) return "";
        String s = raw.trim().toLowerCase();
        s = s.replaceAll("full[\\-_\\s]+stack", "full stack");
        s = s.replaceAll("fullstack", "full stack");
        s = s.replaceAll("[_\\-]", " ");
        return s.replaceAll("\\s+", " ").trim();
    }

    /**
     * Check if a job is role-compatible with any of the candidate's target roles.
     *
     * @param job Active job listing
     * @param candidateRoles Set of candidate roles extracted from candidate's uploaded resume(s)
     * @return true if the job matches at least one candidate role's compatible specialization.
     */
    public static boolean isCompatible(Job job, Set<String> candidateRoles) {
        if (job == null || candidateRoles == null || candidateRoles.isEmpty()) {
            return false;
        }

        String jobTitleNorm = normalizeRole(job.getTitle());

        for (String candidateRole : candidateRoles) {
            String candRoleNorm = normalizeRole(candidateRole);

            // 1. Direct exact or title substring match
            if (jobTitleNorm.equals(candRoleNorm) || jobTitleNorm.contains(candRoleNorm) || candRoleNorm.contains(jobTitleNorm)) {
                return true;
            }

            // 2. Structured taxonomy match
            Set<String> allowedJobRoles = COMPATIBILITY_MAP.get(candRoleNorm);
            if (allowedJobRoles != null) {
                for (String allowed : allowedJobRoles) {
                    String normAllowed = normalizeRole(allowed);
                    if (jobTitleNorm.contains(normAllowed) || normAllowed.contains(jobTitleNorm)) {
                        return true;
                    }
                }
            } else {
                // Generic part match for custom roles (excluding generic words like developer/engineer)
                Set<String> genericWords = Set.of("developer", "engineer", "software", "associate", "senior", "junior", "lead", "manager");
                String[] parts = candRoleNorm.split("\\s+");
                for (String part : parts) {
                    if (part.length() > 3 && !genericWords.contains(part) && jobTitleNorm.contains(part)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
