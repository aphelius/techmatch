package com.techmatch.resume.service.embedding;

import com.techmatch.config.ResumeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeEmbeddingService implements EmbeddingService {

    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final ResumeProperties resumeProperties;

    @Override
    public List<Double> embed(String text) {
        if (!StringUtils.hasText(text)) {
            return fallbackEmbedding("");
        }

        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel != null) {
            try {
                float[] vector = embeddingModel.embed(text);
                List<Double> result = new ArrayList<>(vector.length);
                for (float value : vector) {
                    result.add((double) value);
                }
                return result;
            } catch (Exception exception) {
                log.warn("Embedding model unavailable, falling back to deterministic embedding: {}", exception.getMessage());
            }
        }
        return fallbackEmbedding(text);
    }

    private List<Double> fallbackEmbedding(String text) {
        int dimensions = resumeProperties.getEmbedding().getDimensions();
        byte[] bytes = digest(text);
        List<Double> vector = new ArrayList<>(dimensions);
        for (int index = 0; index < dimensions; index++) {
            int current = bytes[index % bytes.length] & 0xff;
            vector.add((current / 255.0d) * 2 - 1);
        }
        return vector;
    }

    private byte[] digest(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create embedding digest", exception);
        }
    }
}
