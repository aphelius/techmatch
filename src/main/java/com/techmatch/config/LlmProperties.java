package com.techmatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    private String provider;
    private String baseUrl;
    private String apiKey;
    private String chatModel;
    private String embeddingModel;
}
