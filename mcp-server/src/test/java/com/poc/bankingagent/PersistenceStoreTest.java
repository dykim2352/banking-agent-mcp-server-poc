package com.poc.bankingagent;

import com.poc.bankingagent.job.AgentJobStatus;
import org.junit.jupiter.api.Test;

class PersistenceStoreTest extends IntegrationTestSupport {
    @Test
    void agentJobStorePersistsStatusTransitions() {
        var job = jobStore.create("customer_ticket_create", "{\"customerId\":\"CUST-1001\"}");

        org.assertj.core.api.Assertions.assertThat(job.status()).isEqualTo(AgentJobStatus.PENDING);

        jobStore.markRunning(job.jobId());
        org.assertj.core.api.Assertions.assertThat(jobStore.findById(job.jobId()).orElseThrow().status())
                .isEqualTo(AgentJobStatus.RUNNING);

        jobStore.markCompleted(job.jobId(), "{\"ticketId\":\"TICKET-1001\"}");
        var completed = jobStore.findById(job.jobId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(completed.status()).isEqualTo(AgentJobStatus.COMPLETED);
        org.assertj.core.api.Assertions.assertThat(completed.resultPayload()).isEqualTo("{\"ticketId\":\"TICKET-1001\"}");

        var failedJob = jobStore.create("customer_ticket_create", "{\"customerId\":\"CUST-2001\"}");
        jobStore.markFailed(failedJob.jobId(), "LEGACY_TIMEOUT", "Mock legacy timeout");
        var failed = jobStore.findById(failedJob.jobId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(failed.status()).isEqualTo(AgentJobStatus.FAILED);
        org.assertj.core.api.Assertions.assertThat(failed.errorCode()).isEqualTo("LEGACY_TIMEOUT");
    }
}
