package com.careerlens.backend.service;

import java.util.List;

public class CosineSimilarityUtils {

    /**
     * Calculates cosine similarity between vector A and B.
     * Returns a normalized score between 0.0 and 100.0.
     */
    public static double calculateSimilarityScore(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        int size = Math.min(vectorA.size(), vectorB.size());
        if (size == 0) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < size; i++) {
            double a = vectorA.get(i) != null ? vectorA.get(i) : 0.0;
            double b = vectorB.get(i) != null ? vectorB.get(i) : 0.0;

            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA <= 0.0 || normB <= 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        
        // Clamp bounds [-1.0, 1.0] -> [0.0, 100.0]
        if (Double.isNaN(similarity) || Double.isInfinite(similarity)) {
            return 0.0;
        }

        similarity = Math.max(-1.0, Math.min(1.0, similarity));
        
        // Convert to percentage (0 to 100)
        double score = (similarity + 1.0) / 2.0 * 100.0;
        return Math.max(0.0, Math.min(100.0, score));
    }
}
