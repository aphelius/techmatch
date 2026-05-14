package com.techmatch.resume.service.parser;

import com.techmatch.resume.dto.StructuredResumeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ResumeParserAgent {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d[\\d\\- ]{6,}\\d)");
    private static final List<String> SKILL_KEYWORDS = List.of(
            "java", "spring boot", "spring cloud", "mysql", "postgresql", "redis", "rabbitmq",
            "kafka", "docker", "kubernetes", "aws", "linux", "python", "javascript", "typescript",
            "react", "vue", "mybatis", "git", "minio", "elasticsearch"
    );

    public StructuredResumeDto parse(String rawText, String fileName) {
        StructuredResumeDto result = new StructuredResumeDto();
        List<String> lines = Arrays.stream(rawText.split("\\r?\\n"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        populateBasics(result, lines);
        populateSkills(result, rawText);
        populateHighlights(result, lines);

        if (result.getBasics().getName() == null && StringUtils.hasText(fileName)) {
            result.getBasics().setName(fileName.replaceFirst("\\.[^.]+$", ""));
        }

        log.info("ResumeParserAgent parsed resume: name={}, skills={}", result.getBasics().getName(), result.getSkills().size());
        return result;
    }

    private void populateBasics(StructuredResumeDto result, List<String> lines) {
        if (!lines.isEmpty()) {
            result.getBasics().setName(lines.get(0));
        }

        String merged = String.join(" ", lines);
        Matcher emailMatcher = EMAIL_PATTERN.matcher(merged);
        if (emailMatcher.find()) {
            result.getBasics().setEmail(emailMatcher.group());
        }

        Matcher phoneMatcher = PHONE_PATTERN.matcher(merged);
        if (phoneMatcher.find()) {
            result.getBasics().setPhone(phoneMatcher.group(1));
        }

        String summary = lines.stream()
                .filter(line -> line.length() > 20)
                .limit(3)
                .reduce((left, right) -> left + " " + right)
                .orElse(null);
        result.getBasics().setSummary(summary);
    }

    private void populateSkills(StructuredResumeDto result, String rawText) {
        String normalized = rawText.toLowerCase(Locale.ROOT);
        Set<String> skills = new LinkedHashSet<>();
        for (String keyword : SKILL_KEYWORDS) {
            if (normalized.contains(keyword)) {
                skills.add(keyword);
            }
        }
        result.setSkills(skills.stream().toList());
    }

    private void populateHighlights(StructuredResumeDto result, List<String> lines) {
        List<String> highlights = lines.stream()
                .filter(line -> line.startsWith("-") || line.startsWith("*") || line.matches("^\\d+\\..*"))
                .map(line -> line.replaceFirst("^[-*\\d.\\s]+", "").trim())
                .filter(StringUtils::hasText)
                .limit(8)
                .toList();
        result.setHighlights(highlights);
    }
}
