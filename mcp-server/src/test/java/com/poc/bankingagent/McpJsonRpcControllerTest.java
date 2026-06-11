package com.poc.bankingagent;

import com.poc.bankingagent.job.AgentJobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class McpJsonRpcControllerTest extends IntegrationTestSupport {
    @Test
    void toolsListReturnsTools() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"tools/list\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.result.tools[0].name").value("account_summary"))
                .andExpect(jsonPath("$.result.tools[0].requiredRole").value("USER"))
                .andExpect(jsonPath("$.result.tools[0].auditPolicy").value("ALWAYS"))
                .andExpect(jsonPath("$.result.tools[0].inputSchema.required[0]").value("accountId"));
    }

    @Test
    void accountSummaryToolCallReturnsMockData() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Correlation-Id", "test-correlation-001")
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"method\":\"tools/call\",\"params\":{\"name\":\"account_summary\",\"arguments\":{\"accountId\":\"ACC-1001\"}}}"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "test-correlation-001"))
                .andExpect(jsonPath("$.result.content.accountId").value("ACC-1001"))
                .andExpect(jsonPath("$.result.content.customerId").value("CUST-1001"));

        mockMvc.perform(get("/api/v1/audit/events")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].target").value("account_summary"))
                .andExpect(jsonPath("$.events[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.events[0].userId").value("mock-user"));
    }

    @Test
    void accountSummaryToolCallValidatesRequiredArguments() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"3\",\"method\":\"tools/call\",\"params\":{\"name\":\"account_summary\",\"arguments\":{}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value("Missing required argument: accountId"))
                .andExpect(jsonPath("$.error.data.code").value("VALIDATION_ERROR"));
    }

    @Test
    void unsupportedJsonRpcMethodReturnsMethodNotFoundCode() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"method-404\",\"method\":\"tools/unknown\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32601))
                .andExpect(jsonPath("$.error.message").value("Unsupported method: tools/unknown"))
                .andExpect(jsonPath("$.error.data.code").value("METHOD_NOT_FOUND"));
    }

    @Test
    void advisorOnlyToolCallReturnsAccessDeniedForUserAndWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"4\",\"method\":\"tools/call\",\"params\":{\"name\":\"loan_product_recommend\",\"arguments\":{\"customerId\":\"CUST-1001\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("ACCESS_DENIED")))
                .andExpect(jsonPath("$.error.data.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/v1/audit/events")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].target").value("loan_product_recommend"))
                .andExpect(jsonPath("$.events[0].status").value("DENIED"))
                .andExpect(jsonPath("$.events[0].errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.events[0].userId").value("mock-user"));
    }

    @Test
    void toolCallWithoutAuthenticationReturnsUnauthenticatedAndWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"6\",\"method\":\"tools/call\",\"params\":{\"name\":\"account_summary\",\"arguments\":{\"accountId\":\"ACC-1001\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("UNAUTHENTICATED")))
                .andExpect(jsonPath("$.error.data.code").value("UNAUTHENTICATED"));

        mockMvc.perform(get("/api/v1/audit/events")
                        .param("status", "DENIED")
                        .param("target", "account_summary")
                        .param("limit", "1")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].target").value("account_summary"))
                .andExpect(jsonPath("$.events[0].status").value("DENIED"))
                .andExpect(jsonPath("$.events[0].errorCode").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.events[0].userId").value("anonymous"));
    }

    @Test
    void adminOnlyResourceReadReturnsAccessDeniedForUserAndWritesAuditEvent() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"5\",\"method\":\"resources/read\",\"params\":{\"uri\":\"kafka://topics/agent-events\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.message").value(containsString("ACCESS_DENIED")));

        mockMvc.perform(get("/api/v1/audit/events")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].target").value("kafka://topics/agent-events"))
                .andExpect(jsonPath("$.events[0].actionType").value("RESOURCE"))
                .andExpect(jsonPath("$.events[0].status").value("DENIED"));
    }

    @Test
    void asyncJobStatusToolCallReturnsStoredJobStatus() throws Exception {
        var job = jobStore.create("customer_ticket_create", "{\"customerId\":\"CUST-1001\"}");
        jobStore.markRunning(job.jobId());
        jobStore.markCompleted(job.jobId(), "{\"ticketId\":\"TICKET-1001\"}");

        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Api-Key", "mock-user-key")
                        .content("""
                                {"jsonrpc":"2.0","id":"job-1","method":"tools/call","params":{"name":"async_job_status","arguments":{"jobId":"%s"}}}
                                """.formatted(job.jobId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.jobId").value(job.jobId()))
                .andExpect(jsonPath("$.result.content.toolName").value("customer_ticket_create"))
                .andExpect(jsonPath("$.result.content.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result.content.resultPayload").value("{\"ticketId\":\"TICKET-1001\"}"));
    }

    @Test
    void asyncJobStatusToolCallReturnsNotFoundForUnknownJob() throws Exception {
        mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"job-404\",\"method\":\"tools/call\",\"params\":{\"name\":\"async_job_status\",\"arguments\":{\"jobId\":\"missing-job\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").value(-32000))
                .andExpect(jsonPath("$.error.data.code").value("NOT_FOUND"));
    }

    @Test
    void asyncTicketCreateToolCallCreatesPendingJob() throws Exception {
        MvcResult result = mockMvc.perform(post("/mcp")
                        .contentType("application/json")
                        .header("X-Api-Key", "mock-advisor-key")
                        .content("""
                                {"jsonrpc":"2.0","id":"job-create-1","method":"tools/call","params":{"name":"customer_ticket_create_async","arguments":{"customerId":"CUST-1001","title":"Mock title","description":"Mock description"}}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content.toolName").value("customer_ticket_create"))
                .andExpect(jsonPath("$.result.content.status").value("PENDING"))
                .andReturn();

        String jobId = objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/result/content/jobId")
                .asText();

        var storedJob = jobStore.findById(jobId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(storedJob.status()).isEqualTo(AgentJobStatus.PENDING);
        org.assertj.core.api.Assertions.assertThat(storedJob.requestPayload()).contains("CUST-1001");
    }
}
