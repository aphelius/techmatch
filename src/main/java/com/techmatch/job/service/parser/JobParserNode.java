package com.techmatch.job.service.parser;

import com.techmatch.job.dto.CreateJobRequest;
import com.techmatch.job.dto.JobStructuredData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobParserNode {

    private final JobParserAgent jobParserAgent;

    public JobStructuredData parse(CreateJobRequest request) {
        return jobParserAgent.parse(request);
    }
}
