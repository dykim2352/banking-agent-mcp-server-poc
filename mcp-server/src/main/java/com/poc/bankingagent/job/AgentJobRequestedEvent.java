package com.poc.bankingagent.job;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record AgentJobRequestedEvent(
        String jobId,
        String correlationId,
        String toolName,
        JsonNode arguments,
        Instant requestedAt
) {
}
