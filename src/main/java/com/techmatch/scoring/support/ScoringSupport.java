package com.techmatch.scoring.support;

import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.resume.dto.StructuredResumeDto;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScoringSupport {

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d+)\\s*(?:年|years?)", Pattern.CASE_INSENSITIVE);

    private ScoringSupport() {
    }

    public static Set<String> normalizeSet(List<String> values) {
        Set<String> result = new LinkedHashSet<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            String normalized = normalizeText(value);
            if (StringUtils.hasText(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    public static String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String joinResumeCorpus(StructuredResumeDto resumeData, List<RetrievedChunk> retrievedChunks) {
        StringBuilder builder = new StringBuilder();
        if (resumeData.getBasics() != null) {
            append(builder, resumeData.getBasics().getName());
            append(builder, resumeData.getBasics().getSummary());
        }
        if (resumeData.getSkills() != null) {
            resumeData.getSkills().forEach(value -> append(builder, value));
        }
        if (resumeData.getHighlights() != null) {
            resumeData.getHighlights().forEach(value -> append(builder, value));
        }
        if (resumeData.getWorkExperiences() != null) {
            resumeData.getWorkExperiences().forEach(item -> {
                append(builder, item.getCompany());
                append(builder, item.getRole());
                append(builder, item.getPeriod());
                if (item.getHighlights() != null) {
                    item.getHighlights().forEach(value -> append(builder, value));
                }
            });
        }
        if (resumeData.getProjects() != null) {
            resumeData.getProjects().forEach(item -> {
                append(builder, item.getName());
                append(builder, item.getRole());
                append(builder, item.getPeriod());
                if (item.getHighlights() != null) {
                    item.getHighlights().forEach(value -> append(builder, value));
                }
            });
        }
        if (resumeData.getEducation() != null) {
            resumeData.getEducation().forEach(item -> {
                append(builder, item.getSchool());
                append(builder, item.getDegree());
                append(builder, item.getMajor());
                append(builder, item.getPeriod());
            });
        }
        if (retrievedChunks != null) {
            retrievedChunks.forEach(chunk -> append(builder, chunk.getChunkText()));
        }
        return normalizeText(builder.toString());
    }

    public static String joinProjectCorpus(StructuredResumeDto resumeData, List<RetrievedChunk> retrievedChunks) {
        StringBuilder builder = new StringBuilder();
        if (resumeData.getWorkExperiences() != null) {
            resumeData.getWorkExperiences().forEach(item -> {
                append(builder, item.getCompany());
                append(builder, item.getRole());
                if (item.getHighlights() != null) {
                    item.getHighlights().forEach(value -> append(builder, value));
                }
            });
        }
        if (resumeData.getProjects() != null) {
            resumeData.getProjects().forEach(item -> {
                append(builder, item.getName());
                append(builder, item.getRole());
                if (item.getHighlights() != null) {
                    item.getHighlights().forEach(value -> append(builder, value));
                }
            });
        }
        if (retrievedChunks != null) {
            retrievedChunks.forEach(chunk -> append(builder, chunk.getChunkText()));
        }
        return normalizeText(builder.toString());
    }

    public static List<String> collectJobSignals(JobStructuredData jobData) {
        List<String> signals = new ArrayList<>();
        if (jobData.getResponsibilities() != null) {
            signals.addAll(jobData.getResponsibilities());
        }
        if (jobData.getRequirements() != null) {
            signals.addAll(jobData.getRequirements());
        }
        return signals.stream()
                .map(ScoringSupport::normalizeText)
                .filter(StringUtils::hasText)
                .toList();
    }

    public static List<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = normalizeText(text);
        String[] parts = normalized.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}#+.-]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    public static double overlapScore(String source, String target) {
        if (!StringUtils.hasText(source) || !StringUtils.hasText(target)) {
            return 0D;
        }
        String normalizedSource = normalizeText(source);
        String normalizedTarget = normalizeText(target);
        if (normalizedSource.contains(normalizedTarget) || normalizedTarget.contains(normalizedSource)) {
            return 1D;
        }

        Set<String> sourceTokens = new LinkedHashSet<>(extractKeywords(normalizedSource));
        Set<String> targetTokens = new LinkedHashSet<>(extractKeywords(normalizedTarget));
        if (sourceTokens.isEmpty() || targetTokens.isEmpty()) {
            return 0D;
        }

        int hitCount = 0;
        for (String token : sourceTokens) {
            if (targetTokens.contains(token)) {
                hitCount++;
            }
        }
        return (double) hitCount / Math.max(sourceTokens.size(), targetTokens.size());
    }

    public static int estimateYearsFromText(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        Matcher matcher = YEAR_PATTERN.matcher(text);
        int max = 0;
        while (matcher.find()) {
            max = Math.max(max, Integer.parseInt(matcher.group(1)));
        }
        return max;
    }

    private static void append(StringBuilder builder, String value) {
        if (StringUtils.hasText(value)) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(value.trim());
        }
    }
}
