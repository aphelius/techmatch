package com.techmatch.agent.output;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterviewQuestionOutput {
    String type;
    String question;
    String target;
    String difficulty;
}
