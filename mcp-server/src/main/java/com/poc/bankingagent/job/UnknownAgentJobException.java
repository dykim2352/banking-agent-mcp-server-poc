package com.poc.bankingagent.job;

public class UnknownAgentJobException extends RuntimeException {
    public UnknownAgentJobException(String jobId) {
        super("Async job not found: " + jobId);
    }
}
