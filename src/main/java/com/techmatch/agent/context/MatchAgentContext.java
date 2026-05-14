package com.techmatch.agent.context;

import com.techmatch.agent.output.EvidenceVerificationOutput;
import com.techmatch.agent.output.FinalReportOutput;
import com.techmatch.agent.output.HybridScoreResult;
import com.techmatch.agent.output.InterviewQuestionOutput;
import com.techmatch.agent.output.MatchAnalysisOutput;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.resume.dto.StructuredResumeDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MatchAgentContext {

    private Long taskId;
    private Long userId;
    private Long resumeId;
    private Long jobDescriptionId;

    private String resumeFileName;
    private String resumeRawText;
    private StructuredResumeDto resumeData;

    private String jobTitle;
    private String jdRawText;
    private JobStructuredData jobData;

    private List<RetrievedChunk> retrievedChunks = new ArrayList<>();
    private HybridScoreResult hybridScoreResult;
    private MatchAnalysisOutput matchAnalysisOutput;
    private EvidenceVerificationOutput evidenceVerificationOutput;
    private List<InterviewQuestionOutput> interviewQuestions = new ArrayList<>();
    private FinalReportOutput finalReport;

    private List<String> warnings = new ArrayList<>();
    private Map<String, Object> attributes = new HashMap<>();
}
