package com.poc.bankingagent.mcp.service;

import com.poc.bankingagent.audit.AuditService;
import com.poc.bankingagent.audit.AuditStatus;
import com.poc.bankingagent.security.AccessDeniedException;
import com.poc.bankingagent.security.AccessPolicyService;
import com.poc.bankingagent.security.AuthenticationRequiredException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class McpResourceService {
    private final AccessPolicyService accessPolicyService;
    private final AuditService auditService;

    public McpResourceService(AccessPolicyService accessPolicyService, AuditService auditService) {
        this.accessPolicyService = accessPolicyService;
        this.auditService = auditService;
    }

    public Object read(String uri) {
        long startedAt = System.nanoTime();
        try {
            accessPolicyService.assertCanReadResource(uri);
            Object result = switch (uri) {
                case "banking://schemas/account" -> Map.of(
                        "accountId", "string",
                        "customerId", "string",
                        "balance", "decimal",
                        "riskFlags", "array<string>"
                );
                case "banking://schemas/card-transaction" -> Map.of(
                        "transactionId", "string",
                        "approvedAt", "datetime",
                        "merchantName", "string",
                        "amount", "decimal",
                        "status", "string"
                );
                case "banking://policies/access-control" -> "# Access Control Policy\n\n- account_summary: USER, ADVISOR, ADMIN\n- customer_ticket_create: ADVISOR, ADMIN\n- audit resources: ADMIN only\n";
                case "kafka://topics/agent-events" -> "# Kafka Topics\n\n- agent.job.requested\n\nTool 호출 이력은 Kafka로 우회하지 않고 audit_events와 agent_jobs에 직접 저장합니다.\n";
                default -> throw new IllegalArgumentException("Unknown resource: " + uri);
            };
            auditService.record("RESOURCE", uri, AuditStatus.SUCCESS, null, "Resource read completed", elapsedMillis(startedAt));
            return result;
        } catch (AccessDeniedException e) {
            auditService.record("RESOURCE", uri, AuditStatus.DENIED, "ACCESS_DENIED", e.getMessage(), elapsedMillis(startedAt));
            throw e;
        } catch (AuthenticationRequiredException e) {
            auditService.record("RESOURCE", uri, AuditStatus.DENIED, "UNAUTHENTICATED", e.getMessage(), elapsedMillis(startedAt));
            throw e;
        } catch (Exception e) {
            auditService.record("RESOURCE", uri, AuditStatus.FAILED, "RESOURCE_READ_FAILED", e.getMessage(), elapsedMillis(startedAt));
            throw e;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
