package com.poc.bankingagent.job;

import java.util.Optional;

public interface AgentJobStore {
    AgentJob create(String toolName, String requestPayload);

    void markRunning(String jobId);

    void markCompleted(String jobId, String resultPayload);

    void markFailed(String jobId, String errorCode, String errorMessage);

    Optional<AgentJob> findById(String jobId);

    void clear();
}
