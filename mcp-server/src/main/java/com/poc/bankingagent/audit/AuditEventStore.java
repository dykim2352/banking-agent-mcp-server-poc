package com.poc.bankingagent.audit;

import java.util.List;

public interface AuditEventStore {
    void append(AuditEvent event);

    List<AuditEvent> list(String target, AuditStatus status, String correlationId, int limit);

    void clear();
}
