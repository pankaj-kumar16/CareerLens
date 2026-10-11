package com.careerlens.backend.service;

import java.util.List;

public interface EmbeddingService {
    List<Double> generateEmbedding(String text);
    String getProviderName();
    boolean isAvailable();
}
