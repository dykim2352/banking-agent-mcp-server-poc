package com.poc.bankingagent.job;

import com.poc.bankingagent.legacy.dto.TicketResponse;
import com.poc.bankingagent.legacy.service.LegacyBankingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AgentJobWorker {
    private final AgentJobStore jobStore;
    private final LegacyBankingService legacyService;
    private final ObjectMapper objectMapper;

    public AgentJobWorker(AgentJobStore jobStore, LegacyBankingService legacyService, ObjectMapper objectMapper) {
        this.jobStore = jobStore;
        this.legacyService = legacyService;
        this.objectMapper = objectMapper;
    }

    public void process(AgentJobRequestedEvent event) {
        jobStore.markRunning(event.jobId());
        try {
            Object result = switch (event.toolName()) {
                case "customer_ticket_create" -> createTicket(event.arguments());
                default -> throw new IllegalArgumentException("Unsupported async tool: " + event.toolName());
            };
            jobStore.markCompleted(event.jobId(), objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            jobStore.markFailed(event.jobId(), errorCode(e), e.getMessage());
        }
    }

    private TicketResponse createTicket(JsonNode arguments) {
        return legacyService.createTicket(
                required(arguments, "customerId"),
                required(arguments, "title"),
                required(arguments, "description")
        );
    }

    private String required(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("Missing required argument: " + field);
        }
        return node.get(field).asText();
    }

    private String errorCode(Exception e) {
        return e instanceof IllegalArgumentException ? "VALIDATION_ERROR" : "ASYNC_JOB_FAILED";
    }
}
