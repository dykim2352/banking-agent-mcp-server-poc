package com.poc.bankingagent.audit;

import com.poc.bankingagent.security.AuthenticatedPrincipal;
import com.poc.bankingagent.security.AuthenticationContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventStore store;

    public AuditService(AuditEventStore store) {
        this.store = store;
    }

    public void record(String actionType, String target, AuditStatus status, String errorCode, String message, long elapsedMillis) {
        AuthenticatedPrincipal principal = AuthenticationContext.current().orElse(null);
        store.append(new AuditEvent(
                UUID.randomUUID().toString(),
                MDC.get("correlationId"),
                principal == null ? "anonymous" : principal.userId(),
                principal == null ? "UNAUTHENTICATED" : principal.role().name(),
                actionType,
                target,
                status,
                errorCode,
                message,
                Instant.now(),
                elapsedMillis
        ));
    }
}
