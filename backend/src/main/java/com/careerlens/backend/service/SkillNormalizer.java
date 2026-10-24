package com.careerlens.backend.service;

import java.util.*;

public class SkillNormalizer {

    private static final Map<String, String> NORMALIZATION_MAP = new HashMap<>();

    static {
        // Programming languages
        addMapping("JavaScript", "js", "javascript");
        NORMALIZATION_MAP.put("ts", "TypeScript");
        NORMALIZATION_MAP.put("typescript", "TypeScript");
        NORMALIZATION_MAP.put("python3", "Python");
        NORMALIZATION_MAP.put("python", "Python");
        NORMALIZATION_MAP.put("cpp", "C++");
        NORMALIZATION_MAP.put("c++", "C++");
        NORMALIZATION_MAP.put("c#", "C#");
        NORMALIZATION_MAP.put("csharp", "C#");

        // Frameworks & Libraries
        NORMALIZATION_MAP.put("react", "React");
        NORMALIZATION_MAP.put("reactjs", "React");
        NORMALIZATION_MAP.put("react.js", "React");
        NORMALIZATION_MAP.put("node", "Node.js");
        NORMALIZATION_MAP.put("nodejs", "Node.js");
        NORMALIZATION_MAP.put("node.js", "Node.js");
        NORMALIZATION_MAP.put("express", "Express.js");
        NORMALIZATION_MAP.put("expressjs", "Express.js");
        NORMALIZATION_MAP.put("spring", "Spring Boot");
        NORMALIZATION_MAP.put("springboot", "Spring Boot");
        NORMALIZATION_MAP.put("spring boot", "Spring Boot");

        // Databases
        NORMALIZATION_MAP.put("mongo", "MongoDB");
        NORMALIZATION_MAP.put("mongodb", "MongoDB");
        NORMALIZATION_MAP.put("mongo db", "MongoDB");
        NORMALIZATION_MAP.put("postgres", "PostgreSQL");
        NORMALIZATION_MAP.put("postgresql", "PostgreSQL");
        NORMALIZATION_MAP.put("mysql", "MySQL");

        // Cloud & DevOps
        NORMALIZATION_MAP.put("aws", "AWS");
        NORMALIZATION_MAP.put("amazon web services", "AWS");
        NORMALIZATION_MAP.put("k8s", "Kubernetes");
        NORMALIZATION_MAP.put("kubernetes", "Kubernetes");
        NORMALIZATION_MAP.put("docker", "Docker");

        // Web & API
        NORMALIZATION_MAP.put("rest", "REST API");
        NORMALIZATION_MAP.put("rest api", "REST API");
        NORMALIZATION_MAP.put("restful api", "REST API");
        NORMALIZATION_MAP.put("microservices", "Microservices");
        NORMALIZATION_MAP.put("microservice", "Microservices");
        NORMALIZATION_MAP.put("tailwind", "Tailwind CSS");
        NORMALIZATION_MAP.put("tailwindcss", "Tailwind CSS");
    }

    private static void addMapping(String target, String... variants) {
        for (String v : variants) {
            NORMALIZATION_MAP.put(v.toLowerCase(), target);
        }
    }

    public static String normalize(String skill) {
        if (skill == null || skill.trim().isEmpty()) return "";
        String cleaned = skill.trim();
        String lower = cleaned.toLowerCase();
        return NORMALIZATION_MAP.getOrDefault(lower, cleaned);
    }

    public static List<String> normalizeList(List<String> skills) {
        return normalizeList((Collection<String>) skills);
    }

    public static List<String> normalizeList(Collection<String> skills) {
        if (skills == null) return Collections.emptyList();
        Set<String> resultSet = new LinkedHashSet<>();
        for (String s : skills) {
            String norm = normalize(s);
            if (!norm.isEmpty()) {
                resultSet.add(norm);
            }
        }
        return new ArrayList<>(resultSet);
    }
}
