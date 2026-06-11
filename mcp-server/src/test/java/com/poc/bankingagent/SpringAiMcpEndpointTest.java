package com.poc.bankingagent;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpringAiMcpEndpointTest extends IntegrationTestSupport {
    @Test
    void springAiMcpEndpointListsTools() throws Exception {
        String sessionId = initializeSpringAiMcpSession();

        mockMvc.perform(post("/mcp/sdk")
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .header("Mcp-Session-Id", sessionId)
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"sdk-1\",\"method\":\"tools/list\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("account_summary")));
    }

    @Test
    void springAiMcpEndpointCallsAccountSummaryTool() throws Exception {
        String sessionId = initializeSpringAiMcpSession();

        mockMvc.perform(post("/mcp/sdk")
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .header("Mcp-Session-Id", sessionId)
                        .header("X-Api-Key", "mock-user-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"sdk-2\",\"method\":\"tools/call\",\"params\":{\"name\":\"account_summary\",\"arguments\":{\"accountId\":\"ACC-1001\"}}}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ACC-1001")))
                .andExpect(content().string(containsString("CUST-1001")));
    }

    @Test
    void springAiMcpEndpointReadsKafkaTopicResource() throws Exception {
        String sessionId = initializeSpringAiMcpSession("mock-admin-key");

        mockMvc.perform(post("/mcp/sdk")
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                        .header("Mcp-Session-Id", sessionId)
                        .header("X-Api-Key", "mock-admin-key")
                        .content("{\"jsonrpc\":\"2.0\",\"id\":\"sdk-resource-1\",\"method\":\"resources/read\",\"params\":{\"uri\":\"kafka://topics/agent-events\"}}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("kafka://topics/agent-events")))
                .andExpect(content().string(containsString("agent.job.requested")))
                .andExpect(content().string(not(containsString("banking://schemas/card-transaction"))));
    }
}
