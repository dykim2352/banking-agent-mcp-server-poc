package com.poc.bankingagent.mcp.model;

public record McpResource(
        String uri,
        String name,
        String description,
        String mimeType,
        String accessRole,
        String cachePolicy
) {}
