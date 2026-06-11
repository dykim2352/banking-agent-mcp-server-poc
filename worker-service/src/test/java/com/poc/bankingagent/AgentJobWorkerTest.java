package com.poc.bankingagent;

import com.poc.bankingagent.job.AgentJobRequestedConsumer;
import com.poc.bankingagent.job.AgentJobRequestedEvent;
import com.poc.bankingagent.job.AgentJobStatus;
import com.poc.bankingagent.job.UnknownAgentJobException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class AgentJobWorkerTest extends IntegrationTestSupport {
    @Test
    void agentJobWorkerCompletesRequestedTicketJob() throws Exception {
        var arguments = objectMapper.readTree("""
                {"customerId":"CUST-1001","title":"Mock title","description":"Mock description"}
                """);
        var job = jobStore.create("customer_ticket_create", arguments.toString());

        jobWorker.process(new AgentJobRequestedEvent(
                job.jobId(),
                "correlation-job-worker-001",
                "customer_ticket_create",
                arguments,
                Instant.parse("2026-06-10T13:00:00Z")
        ));

        var completed = jobStore.findById(job.jobId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(completed.status()).isEqualTo(AgentJobStatus.COMPLETED);
        org.assertj.core.api.Assertions.assertThat(completed.resultPayload()).contains("TCK-");
    }

    @Test
    void agentJobWorkerMarksFailedWhenRequiredArgumentIsMissing() throws Exception {
        var arguments = objectMapper.readTree("""
                {"customerId":"CUST-1001","title":"Mock title"}
                """);
        var job = jobStore.create("customer_ticket_create", arguments.toString());

        jobWorker.process(new AgentJobRequestedEvent(
                job.jobId(),
                "correlation-job-worker-002",
                "customer_ticket_create",
                arguments,
                Instant.parse("2026-06-10T13:00:00Z")
        ));

        var failed = jobStore.findById(job.jobId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(failed.status()).isEqualTo(AgentJobStatus.FAILED);
        org.assertj.core.api.Assertions.assertThat(failed.errorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void agentJobWorkerThrowsWhenJobIdIsUnknown() throws Exception {
        var arguments = objectMapper.readTree("""
                {"customerId":"CUST-1001","title":"Mock title","description":"Mock description"}
                """);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jobWorker.process(new AgentJobRequestedEvent(
                        "missing-job",
                        "correlation-job-worker-003",
                        "customer_ticket_create",
                        arguments,
                        Instant.parse("2026-06-10T13:00:00Z")
                )))
                .isInstanceOf(UnknownAgentJobException.class)
                .hasMessageContaining("missing-job");
    }

    @Test
    void consumerStoresDeadLetterWhenJobIdCannotBeLinked() throws Exception {
        AgentJobRequestedConsumer consumer = new AgentJobRequestedConsumer(objectMapper, jobWorker, deadLetterStore, 1);
        String payload = """
                {
                  "jobId": "missing-job",
                  "correlationId": "correlation-job-consumer-001",
                  "toolName": "customer_ticket_create",
                  "arguments": {
                    "customerId": "CUST-1001",
                    "title": "Mock title",
                    "description": "Mock description"
                  },
                  "requestedAt": "2026-06-10T13:00:00Z"
                }
                """;

        consumer.consume(new ConsumerRecord<>("agent.job.requested", 0, 0L, "missing-job", payload));

        Integer count = jdbcTemplate.queryForObject("""
                select count(*)
                from kafka_dead_letters
                where topic = ? and message_key = ?
                """, Integer.class, "agent.job.requested", "missing-job");
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
    }
}
