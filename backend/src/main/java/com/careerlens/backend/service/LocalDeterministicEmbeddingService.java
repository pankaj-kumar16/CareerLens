package com.careerlens.backend.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service("localEmbeddingService")
public class LocalDeterministicEmbeddingService implements EmbeddingService {

    private static final int VECTOR_DIMENSION = 64;
    private static final List<String> DICTIONARY = Arrays.asList(
            "java", "spring", "boot", "react", "node", "javascript", "typescript", "python", "sql", "mongodb",
            "docker", "kubernetes", "aws", "azure", "gcp", "git", "rest", "api", "microservices", "frontend",
            "backend", "fullstack", "architect", "engineer", "developer", "b.tech", "master", "bachelor", "degree",
            "experience", "years", "senior", "junior", "lead", "cloud", "database", "security", "jwt", "auth",
            "testing", "junit", "maven", "gradle", "ci/cd", "agile", "scrum", "html", "css", "tailwind",
            "redux", "express", "postgres", "mysql", "redis", "linux", "system", "design", "code", "data", "app"
    );

    @Override
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.nCopies(VECTOR_DIMENSION, 0.0);
        }

        String lower = text.toLowerCase();
        Double[] vector = new Double[VECTOR_DIMENSION];
        Arrays.fill(vector, 0.0);

        // Map dictionary terms into vector indices
        for (int i = 0; i < Math.min(DICTIONARY.size(), VECTOR_DIMENSION); i++) {
            String term = DICTIONARY.get(i);
            if (lower.contains(term)) {
                int occurrences = countOccurrences(lower, term);
                vector[i] = (double) occurrences;
            }
        }

        // Add character hashing components for vocabulary outside dictionary
        String[] words = lower.split("\\s+");
        for (String word : words) {
            if (word.length() > 2) {
                int hash = Math.abs(word.hashCode()) % VECTOR_DIMENSION;
                vector[hash] += 0.5;
            }
        }

        // Normalize vector to unit length
        double norm = 0.0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm > 0.0) {
            for (int i = 0; i < VECTOR_DIMENSION; i++) {
                vector[i] = vector[i] / norm;
            }
        }

        return Arrays.asList(vector);
    }

    private int countOccurrences(String text, String term) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(term, idx)) != -1) {
            count++;
            idx += term.length();
        }
        return count;
    }

    @Override
    public String getProviderName() {
        return "LOCAL_DETERMINISTIC";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
