package com.techmatch.interview.service;

import com.techmatch.match.dto.SubmitInterviewFeedbackRequest;
import com.techmatch.match.dto.SubmitInterviewFeedbackResponse;

public interface FeedbackService {

    SubmitInterviewFeedbackResponse submit(Long taskId, Long userId, SubmitInterviewFeedbackRequest request);
}
