package com.poc.bankingagent.mcp.service;

import com.poc.bankingagent.audit.AuditService;
import com.poc.bankingagent.audit.AuditStatus;
import com.poc.bankingagent.common.NotFoundException;
import com.poc.bankingagent.job.AgentJob;
import com.poc.bankingagent.job.AgentJobPublisher;
import com.poc.bankingagent.job.AgentJobStore;
import com.poc.bankingagent.legacy.service.LegacyBankingService;
import com.poc.bankingagent.security.AccessDeniedException;
import com.poc.bankingagent.security.AccessPolicyService;
import com.poc.bankingagent.security.AuthenticationRequiredException;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class McpToolService {
    private final LegacyBankingService legacyService;
    private final AccessPolicyService accessPolicyService;
    private final AuditService auditService;
    private final AgentJobStore jobStore;
    private final AgentJobPublisher jobPublisher;
    private final Counter toolCallCounter;

    public McpToolService(LegacyBankingService legacyService,
                          AccessPolicyService accessPolicyService, AuditService auditService,
                          AgentJobStore jobStore, AgentJobPublisher jobPublisher, MeterRegistry meterRegistry) {
        this.legacyService = legacyService;
        this.accessPolicyService = accessPolicyService;
        this.auditService = auditService;
        this.jobStore = jobStore;
        this.jobPublisher = jobPublisher;
        this.toolCallCounter = Counter.builder("mcp_tool_call_total").description("MCP tool call count").register(meterRegistry);
    }

    public Object call(String name, JsonNode arguments) {
        String correlationId = MDC.get("correlationId");
        long startedAt = System.nanoTime();
        try {
            accessPolicyService.assertCanCallTool(name);
            toolCallCounter.increment();
            Object result = switch (name) {
                case "account_summary" -> legacyService.getAccountSummary(required(arguments, "accountId"));
                case "card_transaction_search" -> legacyService.searchCardTransactions(required(arguments, "cardId"));
                case "loan_product_recommend" -> legacyService.recommendLoanProducts(required(arguments, "customerId"));
                case "customer_ticket_create" -> legacyService.createTicket(required(arguments, "customerId"), required(arguments, "title"), required(arguments, "description"));
                case "customer_ticket_create_async" -> createAsyncTicketJob(correlationId, arguments);
                case "async_job_status" -> asyncJobStatus(required(arguments, "jobId"));
                default -> throw new IllegalArgumentException("Unknown tool: " + name);
            };
            auditService.record("TOOL", name, AuditStatus.SUCCESS, null, "Tool call completed", elapsedMillis(startedAt));
            return result;
        } catch (AccessDeniedException e) {
            auditService.record("TOOL", name, AuditStatus.DENIED, "ACCESS_DENIED", e.getMessage(), elapsedMillis(startedAt));
            throw e;
        } catch (AuthenticationRequiredException e) {
            auditService.record("TOOL", name, AuditStatus.DENIED, "UNAUTHENTICATED", e.getMessage(), elapsedMillis(startedAt));
            throw e;
        } catch (Exception e) {
            auditService.record("TOOL", name, AuditStatus.FAILED, errorCode(e), e.getMessage(), elapsedMillis(startedAt));
            throw e;
        }
    }

    private Map<String, Object> asyncJobStatus(String jobId) {
        AgentJob job = jobStore.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Async job not found: " + jobId));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jobId", job.jobId());
        response.put("toolName", job.toolName());
        response.put("status", job.status().name());
        response.put("requestPayload", job.requestPayload());
        response.put("resultPayload", job.resultPayload());
        response.put("errorCode", job.errorCode());
        response.put("errorMessage", job.errorMessage());
        response.put("createdAt", job.createdAt().toString());
        response.put("updatedAt", job.updatedAt().toString());
        return response;
    }

    private Map<String, Object> createAsyncTicketJob(String correlationId, JsonNode arguments) {
        required(arguments, "customerId");
        required(arguments, "title");
        required(arguments, "description");
        AgentJob job = jobStore.create("customer_ticket_create", arguments.toString());
        jobPublisher.publishJobRequested(correlationId, job.jobId(), job.toolName(), arguments);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jobId", job.jobId());
        response.put("toolName", job.toolName());
        response.put("status", job.status().name());
        response.put("message", "Async job accepted");
        return response;
    }

    private String required(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("Missing required argument: " + field);
        }
        return node.get(field).asText();
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String errorCode(Exception e) {
        if (e instanceof NotFoundException) {
            return "NOT_FOUND";
        }
        return e instanceof IllegalArgumentException ? "VALIDATION_ERROR" : "TOOL_CALL_FAILED";
    }

}
