package com.poc.bankingagent.job;

import java.time.Instant;

public record AgentJob(
        String jobId,
        String toolName,
        AgentJobStatus status,
        String requestPayload,
        String resultPayload,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {
}
