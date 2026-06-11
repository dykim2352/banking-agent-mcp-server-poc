package com.poc.bankingagent.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SpringAiMcpAdapter {
    private final McpToolService toolService;
    private final McpResourceService resourceService;
    private final ObjectMapper objectMapper;

    public SpringAiMcpAdapter(McpToolService toolService, McpResourceService resourceService, ObjectMapper objectMapper) {
        this.toolService = toolService;
        this.resourceService = resourceService;
        this.objectMapper = objectMapper;
    }

    @McpTool(name = "account_summary", description = "계좌 요약 정보를 조회합니다.")
    public McpSchema.CallToolResult accountSummary(
            @McpToolParam(description = "Account ID, e.g. ACC-1001", required = true) String accountId) {
        return callTool("account_summary", Map.of("accountId", accountId));
    }

    @McpTool(name = "card_transaction_search", description = "카드 거래내역을 조회합니다.")
    public McpSchema.CallToolResult cardTransactionSearch(
            @McpToolParam(description = "Card ID, e.g. CARD-1001", required = true) String cardId) {
        return callTool("card_transaction_search", Map.of("cardId", cardId));
    }

    @McpTool(name = "loan_product_recommend", description = "고객 세그먼트 기반 대출 상품 추천 mock을 반환합니다.")
    public McpSchema.CallToolResult loanProductRecommend(
            @McpToolParam(description = "Customer ID, e.g. CUST-1001", required = true) String customerId) {
        return callTool("loan_product_recommend", Map.of("customerId", customerId));
    }

    @McpTool(name = "customer_ticket_create", description = "고객 상담 티켓을 생성합니다.")
    public McpSchema.CallToolResult customerTicketCreate(
            @McpToolParam(description = "Customer ID, e.g. CUST-1001", required = true) String customerId,
            @McpToolParam(description = "Ticket title", required = true) String title,
            @McpToolParam(description = "Ticket description", required = true) String description) {
        return callTool("customer_ticket_create", Map.of(
                "customerId", customerId,
                "title", title,
                "description", description
        ));
    }

    @McpTool(name = "customer_ticket_create_async", description = "고객 상담 티켓 생성을 비동기 작업으로 접수합니다.")
    public McpSchema.CallToolResult customerTicketCreateAsync(
            @McpToolParam(description = "Customer ID, e.g. CUST-1001", required = true) String customerId,
            @McpToolParam(description = "Ticket title", required = true) String title,
            @McpToolParam(description = "Ticket description", required = true) String description) {
        return callTool("customer_ticket_create_async", Map.of(
                "customerId", customerId,
                "title", title,
                "description", description
        ));
    }

    @McpTool(name = "async_job_status", description = "비동기 작업 상태를 조회합니다.")
    public McpSchema.CallToolResult asyncJobStatus(
            @McpToolParam(description = "Async job ID", required = true) String jobId) {
        return callTool("async_job_status", Map.of("jobId", jobId));
    }

    @McpResource(uri = "banking://schemas/account", name = "Account schema", description = "계좌 요약 mock 스키마", mimeType = "application/json")
    public McpSchema.ReadResourceResult accountSchema() {
        return readResource("banking://schemas/account", "application/json");
    }

    @McpResource(uri = "banking://schemas/card-transaction", name = "Card transaction schema", description = "카드 거래 mock 스키마", mimeType = "application/json")
    public McpSchema.ReadResourceResult cardTransactionSchema() {
        return readResource("banking://schemas/card-transaction", "application/json");
    }

    @McpResource(uri = "banking://policies/access-control", name = "Access control policy", description = "Tool별 접근 제어 정책", mimeType = "text/markdown")
    public McpSchema.ReadResourceResult accessControlPolicy() {
        return readResource("banking://policies/access-control", "text/markdown");
    }

    @McpResource(uri = "kafka://topics/agent-events", name = "Kafka topic design", description = "Agent 이벤트 토픽 설계", mimeType = "text/markdown")
    public McpSchema.ReadResourceResult kafkaTopicDesign() {
        return readResource("kafka://topics/agent-events", "text/markdown");
    }

    @McpPrompt(name = "customer-consulting-summary", description = "고객 상담 요약 프롬프트")
    public McpSchema.GetPromptResult customerConsultingSummary(
            @McpArg(name = "customerId", description = "Customer ID", required = true) String customerId,
            @McpArg(name = "question", description = "Consulting question", required = true) String question) {
        return prompt("고객 정보와 최근 상담 이력을 바탕으로 핵심 이슈와 다음 조치사항을 요약해 주세요.");
    }

    @McpPrompt(name = "loan-product-comparison", description = "대출 상품 비교 프롬프트")
    public McpSchema.GetPromptResult loanProductComparison(
            @McpArg(name = "customerId", description = "Customer ID", required = true) String customerId,
            @McpArg(name = "amount", description = "Requested amount", required = true) String amount) {
        return prompt("고객의 세그먼트와 요청 금액을 기준으로 적합한 대출 상품을 비교해 주세요.");
    }

    @McpPrompt(name = "suspicious-transaction-review", description = "이상 거래 검토 프롬프트")
    public McpSchema.GetPromptResult suspiciousTransactionReview(
            @McpArg(name = "cardId", description = "Card ID", required = true) String cardId,
            @McpArg(name = "period", description = "Review period", required = true) String period) {
        return prompt("최근 카드 거래 내역에서 이상 거래 가능성을 검토하고 확인 포인트를 정리해 주세요.");
    }

    private McpSchema.CallToolResult callTool(String name, Map<String, String> arguments) {
        ObjectNode node = objectMapper.createObjectNode();
        arguments.forEach(node::put);
        Object result = toolService.call(name, node);
        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(toJson(result))), false);
    }

    private McpSchema.ReadResourceResult readResource(String uri, String mimeType) {
        Object result = resourceService.read(uri);
        McpSchema.TextResourceContents contents = new McpSchema.TextResourceContents(uri, mimeType, toJson(result));
        return new McpSchema.ReadResourceResult(List.of(contents));
    }

    private McpSchema.GetPromptResult prompt(String text) {
        McpSchema.PromptMessage message = new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent(text)
        );
        return new McpSchema.GetPromptResult(null, List.of(message));
    }

    private String toJson(Object value) {
        if (value instanceof String stringValue) {
            return stringValue;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize MCP response", e);
        }
    }
}
