package com.careerlens.backend.util;

import com.careerlens.backend.model.Job;
import com.careerlens.backend.service.SkillNormalizer;
import org.springframework.util.StringUtils;

import java.util.*;

public class RoleCompatibilityUtils {

    private static final Map<String, Set<String>> COMPATIBILITY_MAP = new LinkedHashMap<>();

    private static final Set<String> DOMAIN_KEYWORDS = Set.of(
            "react", "python", "devops", "sre", "cloud", "kubernetes", "docker", "aws",
            "frontend", "backend", "full stack", "java", "spring", "node", "angular",
            "vue", "django", "fastapi", "golang", "data", "ml", "ai", "security", "qa"
    );

    static {
        // Full Stack Developer
        COMPATIBILITY_MAP.put("full stack developer", Set.of(
            "full stack developer", "full-stack developer", "fullstack developer",
            "full stack", "fullstack", "full stack engineer", "full-stack engineer", "fullstack engineer",
            "mern stack developer", "mean stack developer", "mern stack", "mean stack",
            "full stack software engineer", "full stack web developer", "java full stack", "python full stack",
            "frontend developer", "backend developer", "web developer", "software engineer", "software developer", "react developer"
        ));

        // React Developer
        COMPATIBILITY_MAP.put("react developer", Set.of(
            "react developer", "react engineer", "react js developer", "reactjs developer",
            "frontend developer", "front end developer", "frontend engineer", "front end engineer",
            "ui developer", "web developer", "full stack developer", "full stack engineer",
            "software engineer", "software developer"
        ));

        // Frontend Developer
        COMPATIBILITY_MAP.put("frontend developer", Set.of(
            "frontend developer", "front-end developer", "front end developer", "frontend engineer", "front-end engineer",
            "react developer", "react engineer", "angular developer", "vue developer", "ui developer",
            "web developer", "full stack developer", "full stack engineer", "fullstack developer",
            "software engineer", "software developer"
        ));

        // Backend Developer
        COMPATIBILITY_MAP.put("backend developer", Set.of(
            "backend developer", "back-end developer", "back end developer", "backend engineer", "back-end engineer",
            "full stack developer", "full stack engineer", "fullstack developer",
            "java developer", "java backend developer", "python developer", "python backend developer",
            "node developer", "node.js backend developer", "server-side developer", "api developer",
            "software engineer", "software developer"
        ));

        // Python Developer
        COMPATIBILITY_MAP.put("python developer", Set.of(
            "python developer", "python backend developer", "python engineer", "python software developer",
            "django developer", "fastapi developer", "backend developer", "backend engineer",
            "full stack developer", "software engineer", "software developer", "data engineer"
        ));

        // DevOps Engineer
        COMPATIBILITY_MAP.put("devops engineer", Set.of(
            "devops engineer", "devops", "devsecops engineer", "devsecops",
            "cloud engineer", "cloud infrastructure engineer", "infrastructure engineer", "platform engineer",
            "site reliability engineer", "sre", "kubernetes engineer", "systems engineer", "automation engineer"
        ));

        // Cloud Engineer
        COMPATIBILITY_MAP.put("cloud engineer", Set.of(
            "cloud engineer", "cloud infrastructure engineer", "aws cloud engineer", "azure cloud engineer",
            "gcp cloud engineer", "devops engineer", "devops", "infrastructure engineer", "platform engineer",
            "site reliability engineer", "sre", "cloud architect"
        ));

        // Web Developer
        COMPATIBILITY_MAP.put("web developer", Set.of(
            "web developer", "full stack developer", "full stack engineer", "fullstack developer",
            "frontend developer", "backend developer", "react developer", "software engineer", "software developer"
        ));

        // Java Developer
        COMPATIBILITY_MAP.put("java developer", Set.of(
            "java developer", "java backend developer", "java engineer", "java software developer",
            "backend developer", "full stack developer", "software engineer", "software developer"
        ));

        // Data Analyst
        COMPATIBILITY_MAP.put("data analyst", Set.of(
            "data analyst", "bi analyst", "business intelligence analyst",
            "data analytics specialist", "sql analyst", "reporting analyst",
            "business analyst", "data scientist"
        ));

        // AI/ML Engineer
        COMPATIBILITY_MAP.put("ai/ml engineer", Set.of(
            "ai/ml engineer", "ai/ml", "machine learning engineer", "ml engineer",
            "ai engineer", "generative ai engineer", "nlp engineer", "deep learning engineer",
            "data scientist", "python developer"
        ));

        // Data Scientist
        COMPATIBILITY_MAP.put("data scientist", Set.of(
            "data scientist", "data science", "machine learning engineer", "ml engineer",
            "ai/ml engineer", "ai engineer", "data analyst", "python developer"
        ));

        // Cybersecurity Engineer
        COMPATIBILITY_MAP.put("cybersecurity engineer", Set.of(
            "cybersecurity engineer", "cyber security engineer", "information security engineer",
            "security engineer", "security analyst", "penetration tester", "secops engineer"
        ));

        // Software Engineer (generic baseline)
        COMPATIBILITY_MAP.put("software engineer", Set.of(
            "software engineer", "software developer", "sde", "swe", "programmer",
            "full stack developer", "full stack engineer", "fullstack developer",
            "backend developer", "backend engineer", "frontend developer", "frontend engineer",
            "react developer", "python developer", "java developer", "web developer", "application developer"
        ));
    }

    /**
     * Normalize a role or job title string:
     * - Handle capitalization (lowercase)
     * - Normalize Unicode dashes (en-dash, em-dash), slashes, underscores, colons, brackets, etc.
     * - Normalize variations of full stack, frontend, backend, react, node, devops
     */
    public static String normalizeRole(String raw) {
        if (!StringUtils.hasText(raw)) return "";
        String s = raw.trim().toLowerCase();
        s = s.replaceAll("[\u2013\u2014_\\-/:|&,()+.\\[\\]]", " ");
        s = s.replaceAll("full[\\s]+stack", "full stack");
        s = s.replaceAll("fullstack", "full stack");
        s = s.replaceAll("front[\\s]+end", "frontend");
        s = s.replaceAll("back[\\s]+end", "backend");
        s = s.replaceAll("react[\\s]*js", "react");
        s = s.replaceAll("node[\\s]*js", "node");
        s = s.replaceAll("vue[\\s]*js", "vue");
        s = s.replaceAll("dev[\\s]*ops", "devops");
        s = s.replaceAll("devsecops", "devops");
        return s.replaceAll("\\s+", " ").trim();
    }

    public static boolean isCompatible(Job job, Set<String> candidateRoles) {
        return isCompatible(job, candidateRoles, (Collection<String>) null);
    }

    /**
     * Comprehensive compatibility evaluation supporting taxonomy, stem matching, and technical skill overlap.
     */
    public static boolean isCompatible(Job job, Set<String> candidateRoles, Collection<String> candidateSkills) {
        if (job == null) {
            return false;
        }

        String jobTitleNorm = normalizeRole(job.getTitle());

        // 1. Role compatibility evaluation
        if (candidateRoles != null && !candidateRoles.isEmpty()) {
            for (String candidateRole : candidateRoles) {
                String candRoleNorm = normalizeRole(candidateRole);

                // Direct exact or substring containment
                if (jobTitleNorm.equals(candRoleNorm) ||
                    (!candRoleNorm.isEmpty() && jobTitleNorm.contains(candRoleNorm)) ||
                    (!jobTitleNorm.isEmpty() && candRoleNorm.contains(jobTitleNorm))) {
                    return true;
                }

                // Domain Stem Matching (e.g. "react", "python", "devops", "cloud", "full stack")
                for (String dk : DOMAIN_KEYWORDS) {
                    if (candRoleNorm.contains(dk) && jobTitleNorm.contains(dk)) {
                        return true;
                    }
                }

                // Taxonomy match
                Set<String> allowedJobRoles = COMPATIBILITY_MAP.get(candRoleNorm);
                if (allowedJobRoles != null) {
                    for (String allowed : allowedJobRoles) {
                        String normAllowed = normalizeRole(allowed);
                        if (!normAllowed.isEmpty() && (jobTitleNorm.contains(normAllowed) || normAllowed.contains(jobTitleNorm))) {
                            return true;
                        }
                    }
                }

                // Significant non-generic token overlap for custom titles
                Set<String> genericWords = Set.of("developer", "engineer", "software", "associate", "senior", "junior", "lead", "manager", "specialist");
                String[] parts = candRoleNorm.split("\\s+");
                for (String part : parts) {
                    if (part.length() > 3 && !genericWords.contains(part) && jobTitleNorm.contains(part)) {
                        return true;
                    }
                }
            }
        }

        // 2. Technical Skill Overlap evaluation
        if (candidateSkills != null && !candidateSkills.isEmpty()) {
            Set<String> cleanCandidateSkills = new HashSet<>();
            for (String cs : candidateSkills) {
                if (StringUtils.hasText(cs)) {
                    cleanCandidateSkills.add(cleanSkill(cs));
                }
            }

            Set<String> cleanJobSkills = new HashSet<>();
            if (job.getRequiredSkills() != null) {
                job.getRequiredSkills().stream().filter(StringUtils::hasText).forEach(s -> cleanJobSkills.add(cleanSkill(s)));
            }
            if (job.getPreferredSkills() != null) {
                job.getPreferredSkills().stream().filter(StringUtils::hasText).forEach(s -> cleanJobSkills.add(cleanSkill(s)));
            }

            // Also check technical indicators in job title
            for (String dk : DOMAIN_KEYWORDS) {
                if (jobTitleNorm.contains(dk) && cleanCandidateSkills.contains(dk)) {
                    return true;
                }
            }

            if (!cleanJobSkills.isEmpty()) {
                for (String js : cleanJobSkills) {
                    if (cleanCandidateSkills.contains(js)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static String cleanSkill(String s) {
        if (!StringUtils.hasText(s)) return "";
        return s.trim().toLowerCase()
                .replaceAll("[\u2013\u2014_\\-/:|&,()+.\\[\\]]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
