package com.poc.bankingagent.mcp.service;

import com.poc.bankingagent.mcp.model.McpPrompt;
import com.poc.bankingagent.mcp.model.McpResource;
import com.poc.bankingagent.mcp.model.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class McpRegistry {
    public List<McpTool> tools() {
        return List.of(
                tool("account_summary", "계좌 요약 정보를 조회합니다.",
                        objectSchema(Map.of("accountId", stringSchema("Account ID, e.g. ACC-1001"))), "USER", "ALWAYS", "SYNC", 3000),
                tool("card_transaction_search", "카드 거래내역을 조회합니다.",
                        objectSchema(Map.of("cardId", stringSchema("Card ID, e.g. CARD-1001"))), "USER", "ALWAYS", "SYNC", 3000),
                tool("loan_product_recommend", "고객 세그먼트 기반 대출 상품 추천 mock을 반환합니다.",
                        objectSchema(Map.of("customerId", stringSchema("Customer ID, e.g. CUST-1001"))), "ADVISOR", "ALWAYS", "SYNC", 3000),
                tool("customer_ticket_create", "고객 상담 티켓을 생성합니다.",
                        objectSchema(Map.of("customerId", stringSchema("Customer ID"), "title", stringSchema("Ticket title"), "description", stringSchema("Ticket description"))), "ADVISOR", "ALWAYS", "SYNC", 5000),
                tool("customer_ticket_create_async", "고객 상담 티켓 생성을 비동기 작업으로 접수합니다.",
                        objectSchema(Map.of("customerId", stringSchema("Customer ID"), "title", stringSchema("Ticket title"), "description", stringSchema("Ticket description"))), "ADVISOR", "ALWAYS", "ASYNC", 1000),
                tool("async_job_status", "비동기 작업 상태를 조회합니다.",
                        objectSchema(Map.of("jobId", stringSchema("Async job ID"))), "USER", "ON_FAILURE", "SYNC", 1000)
        );
    }

    public List<McpResource> resources() {
        return List.of(
                new McpResource("banking://schemas/account", "Account schema", "계좌 요약 mock 스키마", "application/json", "USER", "STATIC"),
                new McpResource("banking://schemas/card-transaction", "Card transaction schema", "카드 거래 mock 스키마", "application/json", "USER", "STATIC"),
                new McpResource("banking://policies/access-control", "Access control policy", "Tool별 접근 제어 정책", "text/markdown", "ADMIN", "STATIC"),
                new McpResource("kafka://topics/agent-events", "Kafka topic design", "Agent 이벤트 토픽 설계", "text/markdown", "ADMIN", "STATIC")
        );
    }

    public List<McpPrompt> prompts() {
        return List.of(
                new McpPrompt("customer-consulting-summary", "고객 상담 요약 프롬프트", List.of("customerId", "question"),
                        "고객 정보와 최근 상담 이력을 바탕으로 핵심 이슈와 다음 조치사항을 요약해 주세요.", "CUSTOMER_SERVICE"),
                new McpPrompt("loan-product-comparison", "대출 상품 비교 프롬프트", List.of("customerId", "amount"),
                        "고객의 세그먼트와 요청 금액을 기준으로 적합한 대출 상품을 비교해 주세요.", "LOAN"),
                new McpPrompt("suspicious-transaction-review", "이상 거래 검토 프롬프트", List.of("cardId", "period"),
                        "최근 카드 거래 내역에서 이상 거래 가능성을 검토하고 확인 포인트를 정리해 주세요.", "CARD")
        );
    }

    private McpTool tool(String name, String description, Map<String, Object> inputSchema,
                        String requiredRole, String auditPolicy, String executionType, int timeoutMillis) {
        return new McpTool(name, description, inputSchema, requiredRole, auditPolicy, executionType, timeoutMillis,
                Map.of(
                        "IllegalArgumentException", "VALIDATION_ERROR",
                        "NotFoundException", "LEGACY_NOT_FOUND",
                        "TimeoutException", "LEGACY_TIMEOUT"
                ));
    }

    private Map<String, Object> stringSchema(String description) {
        return Map.of("type", "string", "description", description);
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties) {
        return Map.of("type", "object", "properties", properties, "required", List.copyOf(properties.keySet()));
    }
}
