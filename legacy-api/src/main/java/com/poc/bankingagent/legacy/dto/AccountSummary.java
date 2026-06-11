package com.poc.bankingagent.legacy.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountSummary(
        String accountId,
        String customerId,
        String accountType,
        String status,
        BigDecimal balance,
        String currency,
        List<String> riskFlags
) {}
