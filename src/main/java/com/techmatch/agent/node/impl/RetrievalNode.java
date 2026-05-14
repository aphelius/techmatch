package com.techmatch.agent.node.impl;

import com.techmatch.agent.context.MatchAgentContext;
import com.techmatch.agent.node.AgentNode;
import com.techmatch.agent.node.AgentNodeResult;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.resume.entity.DocumentChunkEntity;
import com.techmatch.resume.repository.PgvectorDocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@Order(50)
@RequiredArgsConstructor
public class RetrievalNode implements AgentNode {

    private final PgvectorDocumentChunkRepository documentChunkRepository;

    @Override
    public String name() {
        return "RetrievalNode";
    }

    @Override
    public boolean shouldExecute(MatchAgentContext context) {
        return context.getResumeId() != null && context.getJobData() != null;
    }

    @Override
    public AgentNodeResult execute(MatchAgentContext context) {
        List<DocumentChunkEntity> chunks = documentChunkRepository.findByResumeIdAndUserId(
                context.getResumeId(),
                context.getUserId()
        );
        if (chunks.isEmpty()) {
            return AgentNodeResult.failed(name(), "未找到可检索的简历切片", false);
        }

        Set<String> keywords = new LinkedHashSet<>();
        keywords.addAll(normalize(context.getJobData().getRequiredSkills()));
        keywords.addAll(normalize(context.getJobData().getPreferredSkills()));
        keywords.addAll(normalize(context.getJobData().getBusinessKeywords()));

        List<RetrievedChunk> retrieved = new ArrayList<>();
        for (DocumentChunkEntity chunk : chunks) {
            String normalizedText = normalizeText(chunk.getChunkText());
            int hitCount = 0;
            List<String> hitKeywords = new ArrayList<>();
            for (String keyword : keywords) {
                if (normalizedText.contains(keyword)) {
                    hitCount++;
                    hitKeywords.add(keyword);
                }
            }
            double similarity = keywords.isEmpty() ? 0.4 : Math.min(0.98, (double) hitCount / keywords.size() + 0.2);
            if (hitCount > 0 || retrieved.size() < 3) {
                retrieved.add(RetrievedChunk.builder()
                        .chunkId(chunk.getId())
                        .chunkIndex(chunk.getChunkIndex())
                        .chunkText(chunk.getChunkText())
                        .similarity(similarity)
                        .reason(hitKeywords.isEmpty() ? "默认保留高优先级简历切片" : "命中关键词: " + String.join(", ", hitKeywords))
                        .build());
            }
        }

        retrieved = retrieved.stream()
                .sorted(Comparator.comparing(RetrievedChunk::getSimilarity).reversed())
                .limit(5)
                .toList();
        context.setRetrievedChunks(retrieved);

        return AgentNodeResult.success(
                name(),
                "resumeChunks + jobKeywords",
                "检索到 %d 个相关证据片段".formatted(retrieved.size()),
                Map.of("retrievedCount", retrieved.size(), "keywordCount", keywords.size())
        );
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(this::normalizeText)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
