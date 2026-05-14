package com.techmatch.job.service.parser;

import com.techmatch.job.dto.CreateJobRequest;
import com.techmatch.job.dto.JobStructuredData;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class JobParserAgent {

    private static final List<String> SKILL_KEYWORDS = List.of(
            "java", "spring boot", "spring cloud", "mysql", "postgresql", "redis", "rabbitmq",
            "kafka", "docker", "kubernetes", "aws", "linux", "python", "javascript", "typescript",
            "react", "vue", "mybatis", "git", "minio", "elasticsearch", "llm", "rag",
            "embeddings", "vector search", "ai agent", "prompt engineering"
    );

    private static final List<String> BUSINESS_KEYWORDS = List.of(
            "招聘", "recruit", "hr", "talent", "电商", "e-commerce", "金融", "finance",
            "saas", "b2b", "教育", "edtech", "医疗", "healthcare", "物流", "logistics"
    );

    private static final List<String> RESPONSIBILITY_MARKERS = List.of(
            "负责", "职责", "你将", "you will", "responsible", "ownership"
    );

    private static final List<String> REQUIREMENT_MARKERS = List.of(
            "要求", "资格", "任职", "must", "required", "qualification", "需要"
    );

    private static final List<String> PREFERRED_MARKERS = List.of(
            "加分", "优先", "preferred", "plus", "nice to have"
    );

    private static final Pattern BULLET_PREFIX = Pattern.compile("^[-*•\\d.\\s]+");

    public JobStructuredData parse(CreateJobRequest request) {
        JobStructuredData result = new JobStructuredData();
        result.setTitle(trimToNull(request.getTitle()));
        result.setCompany(trimToNull(request.getCompany()));
        result.setLocation(trimToNull(request.getLocation()));

        String rawText = request.getRawText().trim();
        List<String> lines = rawText.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        result.setSummary(lines.stream()
                .filter(line -> line.length() > 20)
                .limit(3)
                .reduce((left, right) -> left + " " + right)
                .orElse(result.getTitle()));

        populateSkills(result, rawText, lines);
        result.setResponsibilities(extractSectionLines(lines, RESPONSIBILITY_MARKERS, 8));
        result.setRequirements(extractSectionLines(lines, REQUIREMENT_MARKERS, 8));
        result.setBusinessKeywords(extractKeywords(rawText, BUSINESS_KEYWORDS));

        if (result.getRequirements().isEmpty()) {
            result.setRequirements(lines.stream()
                    .filter(line -> line.length() > 12)
                    .limit(5)
                    .toList());
        }

        if (result.getResponsibilities().isEmpty()) {
            result.setResponsibilities(lines.stream()
                    .filter(line -> line.length() > 12)
                    .skip(Math.min(1, lines.size()))
                    .limit(5)
                    .toList());
        }

        return result;
    }

    private void populateSkills(JobStructuredData result, String rawText, List<String> lines) {
        String normalized = rawText.toLowerCase(Locale.ROOT);
        Set<String> required = new LinkedHashSet<>();
        Set<String> preferred = new LinkedHashSet<>();

        for (String line : lines) {
            String lowerLine = line.toLowerCase(Locale.ROOT);
            for (String skill : SKILL_KEYWORDS) {
                if (!lowerLine.contains(skill)) {
                    continue;
                }
                if (containsAny(lowerLine, PREFERRED_MARKERS)) {
                    preferred.add(skill);
                } else {
                    required.add(skill);
                }
            }
        }

        if (required.isEmpty() && preferred.isEmpty()) {
            required.addAll(extractKeywords(normalized, SKILL_KEYWORDS));
        }

        preferred.removeAll(required);
        result.setRequiredSkills(new ArrayList<>(required));
        result.setPreferredSkills(new ArrayList<>(preferred));
    }

    private List<String> extractSectionLines(List<String> lines, List<String> markers, int limit) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            String cleaned = BULLET_PREFIX.matcher(line).replaceFirst("").trim();
            if (!StringUtils.hasText(cleaned)) {
                continue;
            }
            if (containsAny(cleaned.toLowerCase(Locale.ROOT), markers) || BULLET_PREFIX.matcher(line).find()) {
                result.add(cleaned);
            }
            if (result.size() >= limit) {
                break;
            }
        }
        return result;
    }

    private List<String> extractKeywords(String text, List<String> keywords) {
        String normalized = text.toLowerCase(Locale.ROOT);
        Set<String> result = new LinkedHashSet<>();
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                result.add(keyword);
            }
        }
        return new ArrayList<>(result);
    }

    private boolean containsAny(String text, List<String> markers) {
        for (String marker : markers) {
            if (text.contains(marker.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
