package com.poc.bankingagent;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpsControllerTest extends IntegrationTestSupport {
    @Test
    void deadLettersApiRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/ops/dead-letters")
                        .header("X-Api-Key", "mock-user-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void jobsApiRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/ops/jobs")
                        .header("X-Api-Key", "mock-user-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void deadLettersApiReturnsStoredDeadLetters() throws Exception {
        deadLetterStore.append("agent.job.requested", "dead-letter-key", "{bad-json}", "JsonParseException: invalid", 3);

        mockMvc.perform(get("/api/v1/ops/dead-letters")
                        .param("topic", "agent.job.requested")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deadLetters[0].topic").value("agent.job.requested"))
                .andExpect(jsonPath("$.deadLetters[0].messageKey").value("dead-letter-key"))
                .andExpect(jsonPath("$.deadLetters[0].retryCount").value(3));
    }

    @Test
    void jobsApiReturnsStoredJobsWithStatusFilter() throws Exception {
        var pendingJob = jobStore.create("customer_ticket_create", "{\"customerId\":\"CUST-1001\"}");
        var completedJob = jobStore.create("customer_ticket_create", "{\"customerId\":\"CUST-2001\"}");
        jobStore.markCompleted(completedJob.jobId(), "{\"ticketId\":\"TCK-1001\"}");

        mockMvc.perform(get("/api/v1/ops/jobs")
                        .param("status", "COMPLETED")
                        .param("limit", "10")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobs[0].jobId").value(completedJob.jobId()))
                .andExpect(jsonPath("$.jobs[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.jobs[0].resultPayload").value("{\"ticketId\":\"TCK-1001\"}"))
                .andExpect(jsonPath("$.jobs[0].jobId").value(org.hamcrest.Matchers.not(pendingJob.jobId())));
    }

    @Test
    void jobApiReturnsSingleStoredJob() throws Exception {
        var job = jobStore.create("customer_ticket_create", "{\"customerId\":\"CUST-1001\"}");

        mockMvc.perform(get("/api/v1/ops/jobs/{jobId}", job.jobId())
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(job.jobId()))
                .andExpect(jsonPath("$.toolName").value("customer_ticket_create"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void jobApiReturnsNotFoundForUnknownJob() throws Exception {
        mockMvc.perform(get("/api/v1/ops/jobs/{jobId}", "missing-job")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
