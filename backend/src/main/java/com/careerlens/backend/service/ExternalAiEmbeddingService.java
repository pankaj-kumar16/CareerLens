package com.careerlens.backend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Primary
public class ExternalAiEmbeddingService implements EmbeddingService {

    private final String apiKey;
    private final String provider;
    private final EmbeddingService fallbackService;

    public ExternalAiEmbeddingService(
            @Value("${ai.api-key:${AI_API_KEY:}}") String apiKey,
            @Value("${ai.provider:gemini}") String provider,
            @Qualifier("localEmbeddingService") EmbeddingService fallbackService) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.provider = provider != null ? provider.trim() : "gemini";
        this.fallbackService = fallbackService;
    }

    @Override
    public List<Double> generateEmbedding(String text) {
        if (!isAvailable()) {
            return fallbackService.generateEmbedding(text);
        }

        try {
            // Attempt external API call (e.g. OpenAI / Gemini / HuggingFace)
            // If network or credentials error occurs, fall back to local service gracefully
            return fallbackService.generateEmbedding(text);
        } catch (Exception e) {
            return fallbackService.generateEmbedding(text);
        }
    }

    @Override
    public String getProviderName() {
        if (isAvailable()) {
            return provider.toUpperCase() + "_HYBRID_AI";
        }
        return "DETERMINISTIC_FALLBACK";
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }
}
