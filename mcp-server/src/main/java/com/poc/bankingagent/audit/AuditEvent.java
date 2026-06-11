package com.poc.bankingagent.audit;

import java.time.Instant;

public record AuditEvent(
        String id,
        String correlationId,
        String userId,
        String role,
        String actionType,
        String target,
        AuditStatus status,
        String errorCode,
        String message,
        Instant timestamp,
        long elapsedMillis
) {
}
