package com.techmatch.scoring.engine;

import com.techmatch.agent.output.HybridScoreResult;
import com.techmatch.agent.output.RetrievedChunk;
import com.techmatch.job.dto.JobStructuredData;
import com.techmatch.resume.dto.StructuredResumeDto;

import java.util.List;

public interface HybridScoringEngine {

    HybridScoreResult score(StructuredResumeDto resumeData,
                            JobStructuredData jobData,
                            List<RetrievedChunk> retrievedChunks,
                            List<String> warnings);
}
