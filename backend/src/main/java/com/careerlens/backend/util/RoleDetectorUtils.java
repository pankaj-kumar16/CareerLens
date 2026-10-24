package com.careerlens.backend.util;

import com.careerlens.backend.model.Resume;
import org.springframework.util.StringUtils;

import java.util.*;

public class RoleDetectorUtils {

    private static final Map<String, List<String>> ROLE_KEYWORDS = new LinkedHashMap<>();

    static {
        // Priority Order: Specific roles evaluated in headline & content
        ROLE_KEYWORDS.put("AI/ML Engineer", List.of("ai/ml engineer", "ai/ml", "machine learning engineer", "ml engineer", "ai engineer", "generative ai engineer", "nlp engineer", "deep learning engineer"));
        ROLE_KEYWORDS.put("Cybersecurity Engineer", List.of("cybersecurity engineer", "cyber security engineer", "information security engineer", "security engineer", "penetration tester", "secops engineer"));
        ROLE_KEYWORDS.put("DevOps Engineer", List.of("devops engineer", "devops", "devsecops engineer", "devsecops", "cloud engineer", "cloud infrastructure engineer", "infrastructure engineer", "platform engineer", "site reliability engineer", "sre", "kubernetes engineer", "automation engineer"));
        ROLE_KEYWORDS.put("Cloud Engineer", List.of("cloud engineer", "cloud infrastructure engineer", "aws engineer", "azure engineer", "gcp engineer", "cloud architect"));
        ROLE_KEYWORDS.put("Full Stack Developer", List.of("full-stack developer", "full stack developer", "fullstack developer", "full-stack", "full stack", "fullstack", "mern stack", "mean stack", "end-to-end developer"));
        ROLE_KEYWORDS.put("Python Developer", List.of("python developer", "python backend developer", "python engineer", "django developer", "fastapi developer", "python software developer"));
        ROLE_KEYWORDS.put("Data Scientist", List.of("data scientist", "data science"));
        ROLE_KEYWORDS.put("Data Engineer", List.of("data engineer", "data pipeline engineer", "big data engineer", "etl developer"));
        ROLE_KEYWORDS.put("Data Analyst", List.of("data analyst", "bi analyst", "business intelligence analyst", "data analytics", "sql analyst"));
        ROLE_KEYWORDS.put("Frontend Developer", List.of("react developer", "react engineer", "react.js developer", "reactjs developer", "frontend developer", "front-end developer", "ui developer", "frontend engineer", "front-end engineer", "angular developer", "vue developer", "web developer"));
        ROLE_KEYWORDS.put("Backend Developer", List.of("backend developer", "back-end developer", "server-side developer", "backend engineer", "back-end engineer", "api developer", "java developer", "node developer", "node.js developer"));
        ROLE_KEYWORDS.put("QA Engineer", List.of("qa engineer", "quality assurance engineer", "test engineer", "automation tester"));
        ROLE_KEYWORDS.put("Mobile Developer", List.of("mobile developer", "android developer", "ios developer", "flutter developer", "react native developer"));
        ROLE_KEYWORDS.put("UI/UX Designer", List.of("ui/ux designer", "ui designer", "ux designer", "product designer"));
        ROLE_KEYWORDS.put("Software Engineer", List.of("software engineer", "software developer", "sde", "swe", "full-time software engineer"));
    }

    private static final List<String> FRONTEND_INDICATORS = List.of("react", "react.js", "reactjs", "angular", "vue", "html", "html5", "css", "css3", "javascript", "js", "bootstrap", "tailwind", "next.js", "frontend", "front-end", "redux");
    private static final List<String> BACKEND_INDICATORS = List.of("node.js", "node", "express", "express.js", "java", "spring", "spring boot", "python", "django", "flask", "fastapi", "c#", ".net", "mongodb", "sql", "postgresql", "mysql", "rest api", "rest apis", "backend", "back-end", "microservices");
    private static final List<String> DEVOPS_INDICATORS = List.of("docker", "kubernetes", "k8s", "aws", "terraform", "ci/cd", "jenkins", "ansible", "linux", "devops", "cloud", "azure", "gcp", "bash", "yaml");
    private static final List<String> DATA_INDICATORS = List.of("sql", "pandas", "numpy", "power bi", "tableau", "excel", "data analysis", "statistics", "data analyst", "data cleaning", "data visualization");
    private static final List<String> AI_INDICATORS = List.of("machine learning", "deep learning", "nlp", "tensorflow", "pytorch", "transformers", "scikit-learn", "hugging face", "llm", "large language models");

    public static String detectRole(String title, String fileName, List<String> skills, String summary, String text) {
        StringBuilder headlineProbe = new StringBuilder();
        if (StringUtils.hasText(title)) headlineProbe.append(title).append(" ");
        if (StringUtils.hasText(fileName)) headlineProbe.append(fileName).append(" ");

        String headlineText = cleanString(headlineProbe.toString());

        // 1. Direct High-Priority Role check on headline
        if (headlineText.contains("full stack") || headlineText.contains("fullstack") ||
            headlineText.contains("mern stack") || headlineText.contains("mean stack") ||
            headlineText.contains("mern developer") || headlineText.contains("mean developer")) {
            return "Full Stack Developer";
        }

        // Check headline against specific keywords (excluding generic Software Engineer)
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            if ("Software Engineer".equals(entry.getKey())) continue;
            for (String kw : entry.getValue()) {
                String normKw = cleanString(kw);
                if (headlineText.contains(normKw)) {
                    return entry.getKey();
                }
            }
        }

        // 2. Full text probe
        StringBuilder fullProbe = new StringBuilder(headlineProbe);
        if (skills != null) fullProbe.append(String.join(" ", skills)).append(" ");
        if (StringUtils.hasText(summary)) fullProbe.append(summary).append(" ");
        if (StringUtils.hasText(text)) fullProbe.append(text);

        String fullText = cleanString(fullProbe.toString());

        if (fullText.contains("full stack") || fullText.contains("fullstack") ||
            fullText.contains("mern stack") || fullText.contains("mean stack") ||
            fullText.contains("mern developer") || fullText.contains("mean developer")) {
            return "Full Stack Developer";
        }

        // 3. Technical Skill Indicator evaluation
        Set<String> lowerSkills = new HashSet<>();
        if (skills != null) {
            skills.forEach(s -> lowerSkills.add(cleanString(s)));
        }
        if (StringUtils.hasText(text)) {
            String textClean = cleanString(text);
            for (String fe : FRONTEND_INDICATORS) {
                if (textClean.contains(fe)) lowerSkills.add(fe);
            }
            for (String be : BACKEND_INDICATORS) {
                if (textClean.contains(be)) lowerSkills.add(be);
            }
            for (String d : DEVOPS_INDICATORS) {
                if (textClean.contains(d)) lowerSkills.add(d);
            }
        }

        boolean hasFrontend = lowerSkills.stream().anyMatch(s -> s.contains("react") || s.contains("html") || s.contains("css") || s.contains("angular") || s.contains("vue") || s.contains("javascript") || s.contains("frontend") || s.contains("front end"));
        boolean hasBackend = lowerSkills.stream().anyMatch(s -> s.contains("node") || s.contains("express") || s.contains("java") || s.contains("spring") || s.contains("django") || s.contains("fastapi") || s.contains("flask") || s.contains("backend") || s.contains("back end") || s.contains("microservices") || s.contains("rest api"));
        long devopsCount = lowerSkills.stream().filter(s -> s.contains("docker") || s.contains("kubernetes") || s.contains("aws") || s.contains("terraform") || s.contains("ci/cd") || s.contains("jenkins") || s.contains("linux") || s.contains("devops")).count();

        if (hasFrontend && hasBackend) {
            return "Full Stack Developer";
        }

        if (devopsCount >= 3 || lowerSkills.stream().anyMatch(s -> s.contains("devops") || s.contains("kubernetes") || s.contains("terraform"))) {
            return "DevOps Engineer";
        }

        // 4. Role keyword search in full text
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            if ("Software Engineer".equals(entry.getKey())) continue;
            for (String kw : entry.getValue()) {
                String normKw = cleanString(kw);
                if (fullText.contains(normKw)) {
                    return entry.getKey();
                }
            }
        }

        if (hasFrontend) return "Frontend Developer";
        if (hasBackend) return "Backend Developer";

        return "Software Engineer";
    }

    public static String detectRole(Resume resume) {
        if (resume == null) return "Software Engineer";
        return detectRole(resume.getTitle(), resume.getOriginalFileName(), resume.getSkills(), resume.getSummary(), resume.getExtractedText());
    }

    /**
     * Extracts ALL qualified roles for a candidate across headline, skills, and resume content.
     */
    public static Set<String> detectAllRoles(Resume resume) {
        if (resume == null) return Set.of("software engineer");
        return detectAllRoles(resume.getTitle(), resume.getOriginalFileName(), resume.getSkills(), resume.getSummary(), resume.getExtractedText());
    }

    public static Set<String> detectAllRoles(String title, String fileName, List<String> skills, String summary, String text) {
        Set<String> roles = new LinkedHashSet<>();

        // Always include the primary detected role
        String primaryRole = detectRole(title, fileName, skills, summary, text);
        if (StringUtils.hasText(primaryRole)) {
            roles.add(primaryRole.toLowerCase());
        }

        StringBuilder probe = new StringBuilder();
        if (StringUtils.hasText(title)) probe.append(title).append(" ");
        if (StringUtils.hasText(fileName)) probe.append(fileName).append(" ");
        if (skills != null) probe.append(String.join(" ", skills)).append(" ");
        if (StringUtils.hasText(summary)) probe.append(summary).append(" ");
        if (StringUtils.hasText(text)) probe.append(text);

        String fullText = cleanString(probe.toString());

        // Full Stack indicators
        if (fullText.contains("full stack") || fullText.contains("fullstack") || fullText.contains("mern stack") || fullText.contains("mean stack")) {
            roles.add("full stack developer");
            roles.add("software engineer");
            roles.add("frontend developer");
            roles.add("backend developer");
        }

        // Dual skill competency
        Set<String> lowerSkills = new HashSet<>();
        if (skills != null) skills.forEach(s -> lowerSkills.add(cleanString(s)));
        if (StringUtils.hasText(text)) {
            String textClean = cleanString(text);
            for (String fe : FRONTEND_INDICATORS) {
                if (textClean.contains(fe)) lowerSkills.add(fe);
            }
            for (String be : BACKEND_INDICATORS) {
                if (textClean.contains(be)) lowerSkills.add(be);
            }
            for (String d : DEVOPS_INDICATORS) {
                if (textClean.contains(d)) lowerSkills.add(d);
            }
        }

        boolean hasFrontend = lowerSkills.stream().anyMatch(s -> s.contains("react") || s.contains("html") || s.contains("css") || s.contains("angular") || s.contains("vue") || s.contains("javascript") || s.contains("frontend") || s.contains("front end"));
        boolean hasBackend = lowerSkills.stream().anyMatch(s -> s.contains("node") || s.contains("express") || s.contains("java") || s.contains("spring") || s.contains("django") || s.contains("fastapi") || s.contains("flask") || s.contains("backend") || s.contains("back end") || s.contains("microservices") || s.contains("rest api"));
        long devopsCount = lowerSkills.stream().filter(s -> s.contains("docker") || s.contains("kubernetes") || s.contains("aws") || s.contains("terraform") || s.contains("ci/cd") || s.contains("jenkins") || s.contains("linux") || s.contains("devops")).count();

        if (hasFrontend && hasBackend) {
            roles.add("full stack developer");
            roles.add("software engineer");
            roles.add("frontend developer");
            roles.add("backend developer");
            roles.add("web developer");
        } else {
            if (hasFrontend) {
                roles.add("frontend developer");
                roles.add("web developer");
                roles.add("software engineer");
            }
            if (hasBackend) {
                roles.add("backend developer");
                roles.add("software engineer");
            }
        }

        if (devopsCount >= 2 || lowerSkills.stream().anyMatch(s -> s.contains("devops") || s.contains("kubernetes") || s.contains("terraform"))) {
            roles.add("devops engineer");
            roles.add("cloud engineer");
            roles.add("site reliability engineer");
        }

        if (lowerSkills.stream().anyMatch(s -> s.contains("react"))) {
            roles.add("react developer");
            roles.add("frontend developer");
        }

        if (lowerSkills.stream().anyMatch(s -> s.contains("python"))) {
            roles.add("python developer");
        }

        if (lowerSkills.stream().anyMatch(s -> s.contains("java") || s.contains("spring"))) {
            roles.add("java developer");
            roles.add("backend developer");
        }

        // Check all other role keywords
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                String normKw = cleanString(kw);
                if (fullText.contains(normKw)) {
                    roles.add(entry.getKey().toLowerCase());
                    break;
                }
            }
        }

        // If no specific role was detected across patterns, default to software engineer
        if (roles.isEmpty()) {
            roles.add("software engineer");
        }

        return roles;
    }

    private static String cleanString(String s) {
        if (!StringUtils.hasText(s)) return "";
        return s.toLowerCase()
                .replaceAll("[\u2013\u2014_\\-/:|&,()+]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
