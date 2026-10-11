package com.careerlens.backend.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CosineSimilarityUtilsTest {

    @Test
    void testIdenticalVectorsProduce100() {
        List<Double> v1 = Arrays.asList(1.0, 2.0, 3.0);
        List<Double> v2 = Arrays.asList(1.0, 2.0, 3.0);

        double score = CosineSimilarityUtils.calculateSimilarityScore(v1, v2);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    void testOrthogonalVectorsProduce50() {
        List<Double> v1 = Arrays.asList(1.0, 0.0);
        List<Double> v2 = Arrays.asList(0.0, 1.0);

        double score = CosineSimilarityUtils.calculateSimilarityScore(v1, v2);
        assertEquals(50.0, score, 0.01);
    }

    @Test
    void testEmptyOrNullVectorsHandleGracefully() {
        assertEquals(0.0, CosineSimilarityUtils.calculateSimilarityScore(null, Arrays.asList(1.0, 2.0)));
        assertEquals(0.0, CosineSimilarityUtils.calculateSimilarityScore(Collections.emptyList(), Arrays.asList(1.0, 2.0)));
        assertEquals(0.0, CosineSimilarityUtils.calculateSimilarityScore(Arrays.asList(0.0, 0.0), Arrays.asList(0.0, 0.0)));
    }
}
