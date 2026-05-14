package com.techmatch.resume.service.parser;

import com.techmatch.resume.dto.StructuredResumeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResumeParserNode {

    private final ResumeParserAgent resumeParserAgent;

    public StructuredResumeDto parse(String rawText, String fileName) {
        return resumeParserAgent.parse(rawText, fileName);
    }
}
