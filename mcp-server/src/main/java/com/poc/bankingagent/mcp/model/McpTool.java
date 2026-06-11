package com.poc.bankingagent.mcp.model;

import java.util.Map;

public record McpTool(
        String name,
        String description,
        Map<String, Object> inputSchema,
        String requiredRole,
        String auditPolicy,
        String executionType,
        int timeoutMillis,
        Map<String, String> errorCodeMapping
) {}
