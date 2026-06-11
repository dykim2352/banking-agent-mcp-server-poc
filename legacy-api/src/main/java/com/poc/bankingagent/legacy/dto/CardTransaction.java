package com.poc.bankingagent.legacy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardTransaction(
        String transactionId,
        String cardId,
        LocalDateTime approvedAt,
        String merchantName,
        BigDecimal amount,
        String currency,
        String category,
        String status
) {}
