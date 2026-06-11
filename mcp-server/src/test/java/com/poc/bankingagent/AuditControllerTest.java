package com.poc.bankingagent;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditControllerTest extends IntegrationTestSupport {
    @Test
    void auditEventsApiRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/audit/events")
                        .header("X-Api-Key", "mock-user-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void auditEventsApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/audit/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void auditEventsApiRejectsInvalidStatusFilter() throws Exception {
        mockMvc.perform(get("/api/v1/audit/events")
                        .param("status", "BAD_STATUS")
                        .header("X-Api-Key", "mock-admin-key"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter: status"));
    }
}
