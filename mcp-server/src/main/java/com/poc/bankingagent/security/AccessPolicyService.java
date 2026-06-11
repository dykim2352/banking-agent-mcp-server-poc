package com.poc.bankingagent.security;

import com.poc.bankingagent.mcp.model.McpResource;
import com.poc.bankingagent.mcp.model.McpTool;
import com.poc.bankingagent.mcp.service.McpRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccessPolicyService {
    private static final Map<String, Permission> TOOL_PERMISSIONS = Map.of(
            "account_summary", Permission.TOOL_ACCOUNT_READ,
            "card_transaction_search", Permission.TOOL_CARD_TRANSACTION_READ,
            "loan_product_recommend", Permission.TOOL_LOAN_RECOMMEND,
            "customer_ticket_create", Permission.TOOL_TICKET_CREATE,
            "customer_ticket_create_async", Permission.TOOL_TICKET_CREATE,
            "async_job_status", Permission.TOOL_ASYNC_JOB_READ
    );

    private static final Map<String, Permission> RESOURCE_PERMISSIONS = Map.of(
            "banking://schemas/account", Permission.RESOURCE_SCHEMA_READ,
            "banking://schemas/card-transaction", Permission.RESOURCE_SCHEMA_READ,
            "banking://policies/access-control", Permission.RESOURCE_POLICY_READ,
            "kafka://topics/agent-events", Permission.RESOURCE_KAFKA_TOPIC_READ
    );

    private final McpRegistry registry;

    public AccessPolicyService(McpRegistry registry) {
        this.registry = registry;
    }

    public void assertCanCallTool(String toolName) {
        registry.tools().stream()
                .filter(candidate -> candidate.name().equals(toolName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + toolName));
        assertPermission(requiredToolPermission(toolName), "tool", toolName);
    }

    public void assertCanReadResource(String uri) {
        registry.resources().stream()
                .filter(candidate -> candidate.uri().equals(uri))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown resource: " + uri));
        assertPermission(requiredResourcePermission(uri), "resource", uri);
    }

    public void assertAdmin(String target) {
        assertPermission(requiredAdminPermission(target), "api", target);
    }

    private void assertPermission(Permission permission, String targetType, String target) {
        AuthenticatedPrincipal principal = AuthenticationContext.current()
                .orElseThrow(() -> new AuthenticationRequiredException("mock API key is required for " + targetType + " " + target));

        if (!principal.role().has(permission)) {
            throw new AccessDeniedException(targetType + " " + target + " requires permission " + permission);
        }
    }

    private Permission requiredToolPermission(String toolName) {
        Permission permission = TOOL_PERMISSIONS.get(toolName);
        if (permission == null) {
            throw new IllegalArgumentException("Unknown tool permission: " + toolName);
        }
        return permission;
    }

    private Permission requiredResourcePermission(String uri) {
        Permission permission = RESOURCE_PERMISSIONS.get(uri);
        if (permission == null) {
            throw new IllegalArgumentException("Unknown resource permission: " + uri);
        }
        return permission;
    }

    private Permission requiredAdminPermission(String target) {
        if (target.startsWith("audit")) {
            return Permission.OPS_AUDIT_READ;
        }
        if (target.startsWith("job")) {
            return Permission.OPS_JOB_READ;
        }
        if (target.startsWith("dead")) {
            return Permission.OPS_DEAD_LETTER_READ;
        }
        return Permission.OPS_AUDIT_READ;
    }
}
