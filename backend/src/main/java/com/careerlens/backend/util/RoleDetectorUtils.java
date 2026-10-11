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
        ROLE_KEYWORDS.put("Cloud Engineer", List.of("cloud engineer", "cloud infrastructure engineer", "aws engineer", "azure engineer", "gcp engineer"));
        ROLE_KEYWORDS.put("Full Stack Developer", List.of("full-stack developer", "full stack developer", "fullstack developer", "full-stack", "full stack", "fullstack", "mern stack", "mean stack", "end-to-end developer"));
        ROLE_KEYWORDS.put("DevOps Engineer", List.of("devops engineer", "devsecops engineer", "cloud engineer", "devops", "devsecops", "kubernetes engineer"));
        ROLE_KEYWORDS.put("Python Developer", List.of("python developer", "python backend developer", "python engineer"));
        ROLE_KEYWORDS.put("Data Scientist", List.of("data scientist", "data science"));
        ROLE_KEYWORDS.put("Data Engineer", List.of("data engineer", "data pipeline engineer", "big data engineer", "etl developer"));
        ROLE_KEYWORDS.put("Data Analyst", List.of("data analyst", "bi analyst", "business intelligence analyst", "data analytics", "sql analyst"));
        ROLE_KEYWORDS.put("Backend Developer", List.of("backend developer", "back-end developer", "server-side developer", "backend engineer", "back-end engineer", "api developer"));
        ROLE_KEYWORDS.put("Frontend Developer", List.of("frontend developer", "front-end developer", "ui developer", "frontend engineer", "front-end engineer", "web developer"));
        ROLE_KEYWORDS.put("Software Engineer", List.of("software engineer", "software developer", "sde", "swe", "full-time software engineer"));
        ROLE_KEYWORDS.put("QA Engineer", List.of("qa engineer", "quality assurance engineer", "test engineer", "automation tester"));
        ROLE_KEYWORDS.put("Mobile Developer", List.of("mobile developer", "android developer", "ios developer", "flutter developer", "react native developer"));
        ROLE_KEYWORDS.put("UI/UX Designer", List.of("ui/ux designer", "ui designer", "ux designer", "product designer"));
        ROLE_KEYWORDS.put("Cloud Architect", List.of("cloud architect", "solutions architect", "aws architect", "azure architect"));
    }

    private static final List<String> FRONTEND_INDICATORS = List.of("react", "react.js", "angular", "vue", "html", "html5", "css", "css3", "javascript", "js", "bootstrap", "tailwind", "next.js", "frontend", "front-end", "redux");
    private static final List<String> BACKEND_INDICATORS = List.of("node.js", "node", "express", "express.js", "java", "spring", "spring boot", "python", "django", "flask", "c#", ".net", "mongodb", "sql", "postgresql", "mysql", "rest api", "rest apis", "backend", "back-end", "microservices");

    public static String detectRole(String title, String fileName, List<String> skills, String summary, String text) {
        // 1. Check Headline / Title / Filename first (Highest Priority)
        StringBuilder headlineProbe = new StringBuilder();
        if (StringUtils.hasText(title)) headlineProbe.append(title).append(" ");
        if (StringUtils.hasText(fileName)) headlineProbe.append(fileName).append(" ");

        String headlineText = headlineProbe.toString().toLowerCase().replaceAll("[_\\-]", " ");
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                String normKw = kw.toLowerCase().replaceAll("[_\\-]", " ");
                if (headlineText.contains(normKw)) {
                    return entry.getKey();
                }
            }
        }

        // 2. Check full summary and extracted text
        StringBuilder fullProbe = new StringBuilder(headlineProbe);
        if (skills != null) fullProbe.append(String.join(" ", skills)).append(" ");
        if (StringUtils.hasText(summary)) fullProbe.append(summary).append(" ");
        if (StringUtils.hasText(text)) fullProbe.append(text);

        String fullText = fullProbe.toString().toLowerCase().replaceAll("[_\\-]", " ");
        for (Map.Entry<String, List<String>> entry : ROLE_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                String normKw = kw.toLowerCase().replaceAll("[_\\-]", " ");
                if (fullText.contains(normKw)) {
                    return entry.getKey();
                }
            }
        }

        // 3. Dual Skill Competency Detection (Frontend + Backend -> Full Stack Developer)
        Set<String> lowerSkills = new HashSet<>();
        if (skills != null) {
            skills.forEach(s -> lowerSkills.add(s.toLowerCase()));
        }
        if (StringUtils.hasText(text)) {
            String textLower = text.toLowerCase();
            for (String fe : FRONTEND_INDICATORS) {
                if (textLower.contains(fe.toLowerCase())) lowerSkills.add(fe.toLowerCase());
            }
            for (String be : BACKEND_INDICATORS) {
                if (textLower.contains(be.toLowerCase())) lowerSkills.add(be.toLowerCase());
            }
        }

        boolean hasFrontend = lowerSkills.stream().anyMatch(s -> s.contains("react") || s.contains("html") || s.contains("css") || s.contains("angular") || s.contains("vue") || s.contains("javascript") || s.contains("frontend") || s.contains("front-end"));
        boolean hasBackend = lowerSkills.stream().anyMatch(s -> s.contains("node") || s.contains("express") || s.contains("java") || s.contains("spring") || s.contains("python") || s.contains("mongo") || s.contains("sql") || s.contains("api") || s.contains("backend") || s.contains("back-end"));

        if (hasFrontend && hasBackend) {
            return "Full Stack Developer";
        }
        if (hasFrontend) return "Frontend Developer";
        if (hasBackend) return "Backend Developer";

        return "Software Engineer";
    }

    public static String detectRole(Resume resume) {
        if (resume == null) return "Software Engineer";
        return detectRole(resume.getTitle(), resume.getOriginalFileName(), resume.getSkills(), resume.getSummary(), resume.getExtractedText());
    }
}
