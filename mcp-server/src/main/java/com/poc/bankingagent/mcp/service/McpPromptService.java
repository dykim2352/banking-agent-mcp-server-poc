package com.poc.bankingagent.mcp.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class McpPromptService {
    public Object get(String name, Map<String, Object> arguments) {
        return switch (name) {
            case "customer-consulting-summary" -> Map.of(
                    "name", name,
                    "messages", new Object[]{Map.of("role", "user", "content", "고객 정보와 최근 상담 이력을 바탕으로 핵심 이슈와 다음 조치사항을 요약해 주세요.")}
            );
            case "loan-product-comparison" -> Map.of(
                    "name", name,
                    "messages", new Object[]{Map.of("role", "user", "content", "고객의 세그먼트와 요청 금액을 기준으로 적합한 대출 상품을 비교해 주세요.")}
            );
            case "suspicious-transaction-review" -> Map.of(
                    "name", name,
                    "messages", new Object[]{Map.of("role", "user", "content", "최근 카드 거래 내역에서 이상 거래 가능성을 검토하고 확인 포인트를 정리해 주세요.")}
            );
            default -> throw new IllegalArgumentException("Unknown prompt: " + name);
        };
    }
}
