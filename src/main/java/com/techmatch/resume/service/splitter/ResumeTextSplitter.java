package com.techmatch.resume.service.splitter;

import com.techmatch.config.ResumeProperties;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResumeTextSplitter {

    private final ResumeProperties resumeProperties;

    public List<SplitChunk> split(String rawText) {
        List<SplitChunk> chunks = new ArrayList<>();
        if (!StringUtils.hasText(rawText)) {
            return chunks;
        }

        int maxLength = resumeProperties.getChunk().getMaxLength();
        int overlap = Math.min(resumeProperties.getChunk().getOverlap(), maxLength / 2);
        int start = 0;
        int index = 0;
        String normalized = rawText.replace("\r", "").trim();

        while (start < normalized.length()) {
            int end = Math.min(start + maxLength, normalized.length());
            String content = normalized.substring(start, end).trim();
            if (StringUtils.hasText(content)) {
                chunks.add(SplitChunk.builder()
                        .chunkIndex(index++)
                        .text(content)
                        .startOffset(start)
                        .endOffset(end)
                        .build());
            }
            if (end == normalized.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return chunks;
    }

    @Data
    @Builder
    public static class SplitChunk {
        private Integer chunkIndex;
        private String text;
        private Integer startOffset;
        private Integer endOffset;
    }
}
