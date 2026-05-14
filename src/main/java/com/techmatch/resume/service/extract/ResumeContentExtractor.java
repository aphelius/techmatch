package com.techmatch.resume.service.extract;

import com.techmatch.common.enums.ErrorCode;
import com.techmatch.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResumeContentExtractor {

    private final List<ResumeTextExtractor> extractors;

    public String extract(String extension, byte[] content) {
        ResumeTextExtractor extractor = extractors.stream()
                .filter(candidate -> candidate.supports(extension))
                .findFirst()
                .orElseThrow(() -> new BizException(ErrorCode.FILE_TYPE_NOT_SUPPORTED));

        String text = extractor.extract(content);
        if (!StringUtils.hasText(text)) {
            throw new BizException(ErrorCode.RESUME_PARSE_FAILED.getCode(), "resume text is empty after extraction");
        }
        return text.trim();
    }
}
