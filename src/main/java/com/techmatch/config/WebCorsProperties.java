package com.techmatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.cors")
public class WebCorsProperties {

    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*"
    ));

    private List<String> allowedMethods = new ArrayList<>(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
    ));

    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    private List<String> exposedHeaders = new ArrayList<>(List.of(
            "X-Request-Id"
    ));

    private Boolean allowCredentials = true;
}
