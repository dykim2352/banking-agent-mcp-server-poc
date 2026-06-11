package com.poc.bankingagent.mcp.controller;

import com.poc.bankingagent.common.NotFoundException;
import com.poc.bankingagent.mcp.dto.JsonRpcRequest;
import com.poc.bankingagent.mcp.exception.JsonRpcMethodNotFoundException;
import com.poc.bankingagent.mcp.dto.JsonRpcResponse;
import com.poc.bankingagent.mcp.service.McpPromptService;
import com.poc.bankingagent.mcp.service.McpRegistry;
import com.poc.bankingagent.mcp.service.McpResourceService;
import com.poc.bankingagent.mcp.service.McpToolService;
import com.poc.bankingagent.security.AccessDeniedException;
import com.poc.bankingagent.security.AuthenticationRequiredException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpJsonRpcController {
    private final McpRegistry registry;
    private final McpToolService toolService;
    private final McpResourceService resourceService;
    private final McpPromptService promptService;

    public McpJsonRpcController(McpRegistry registry, McpToolService toolService, McpResourceService resourceService, McpPromptService promptService) {
        this.registry = registry;
        this.toolService = toolService;
        this.resourceService = resourceService;
        this.promptService = promptService;
    }

    @PostMapping
    public JsonRpcResponse handle(@RequestBody JsonRpcRequest request) {
        try {
            if (request.method() == null || request.method().isBlank()) {
                throw new IllegalArgumentException("Missing required field: method");
            }
            Object result = switch (request.method()) {
                case "tools/list" -> Map.of("tools", registry.tools());
                case "tools/call" -> handleToolCall(request.params());
                case "resources/list" -> Map.of("resources", registry.resources());
                case "resources/read" -> handleResourceRead(request.params());
                case "prompts/list" -> Map.of("prompts", registry.prompts());
                case "prompts/get" -> handlePromptGet(request.params());
                default -> throw new JsonRpcMethodNotFoundException(request.method());
            };
            return JsonRpcResponse.ok(request.id(), result);
        } catch (Exception e) {
            String domainCode = domainCode(e);
            return JsonRpcResponse.error(request.id(), jsonRpcCode(e), e.getMessage(), Map.of("code", domainCode));
        }
    }

    private Object handleToolCall(JsonNode params) {
        String name = requiredText(params, "name");
        JsonNode arguments = requiredNode(params, "arguments");
        return Map.of("content", toolService.call(name, arguments));
    }

    private Object handleResourceRead(JsonNode params) {
        String uri = requiredText(params, "uri");
        return Map.of("contents", resourceService.read(uri));
    }

    private Object handlePromptGet(JsonNode params) {
        String name = requiredText(params, "name");
        return promptService.get(name, Map.of());
    }

    private String requiredText(JsonNode params, String field) {
        JsonNode value = requiredNode(params, field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing required param: " + field);
        }
        return value.asText();
    }

    private JsonNode requiredNode(JsonNode params, String field) {
        if (params == null || !params.hasNonNull(field)) {
            throw new IllegalArgumentException("Missing required param: " + field);
        }
        return params.get(field);
    }

    private String domainCode(Exception e) {
        if (e instanceof JsonRpcMethodNotFoundException) {
            return "METHOD_NOT_FOUND";
        }
        if (e instanceof AuthenticationRequiredException) {
            return "UNAUTHENTICATED";
        }
        if (e instanceof AccessDeniedException) {
            return "ACCESS_DENIED";
        }
        if (e instanceof NotFoundException) {
            return "NOT_FOUND";
        }
        if (e instanceof IllegalArgumentException) {
            return "VALIDATION_ERROR";
        }
        return "INTERNAL_ERROR";
    }

    private int jsonRpcCode(Exception e) {
        if (e instanceof JsonRpcMethodNotFoundException) {
            return -32601;
        }
        if (e instanceof IllegalArgumentException) {
            return -32602;
        }
        return -32000;
    }
}
