package com.techmatch.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "resume")
public class ResumeProperties {

    private Upload upload = new Upload();
    private Chunk chunk = new Chunk();
    private Embedding embedding = new Embedding();

    @Data
    public static class Upload {

        @Min(1)
        private long maxFileSizeBytes = 5 * 1024 * 1024L;

        @NotEmpty
        private List<String> allowedExtensions = List.of("pdf", "docx");
    }

    @Data
    public static class Chunk {

        @Min(100)
        private int maxLength = 800;

        @Min(0)
        private int overlap = 120;
    }

    @Data
    public static class Embedding {

        @Min(4)
        private int dimensions = 16;
    }
}
