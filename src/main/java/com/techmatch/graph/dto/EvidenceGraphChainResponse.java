package com.techmatch.graph.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EvidenceGraphChainResponse {
    String requirement;
    String evidence;
    String status;
    String risk;
    String question;
}
