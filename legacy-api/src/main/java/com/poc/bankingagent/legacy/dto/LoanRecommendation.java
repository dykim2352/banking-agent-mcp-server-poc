package com.poc.bankingagent.legacy.dto;

import java.util.List;

public record LoanRecommendation(
        String customerId,
        List<String> productCodes,
        String reason,
        String riskLevel
) {}
