package com.poc.bankingagent.legacy.dto;

import java.time.Instant;

public record TicketResponse(
        String ticketId,
        String customerId,
        String title,
        String status,
        Instant createdAt
) {}
