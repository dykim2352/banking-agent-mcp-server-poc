package com.poc.bankingagent.mcp.model;

import java.util.List;

public record McpPrompt(
        String name,
        String description,
        List<String> arguments,
        String template,
        String domain
) {}
