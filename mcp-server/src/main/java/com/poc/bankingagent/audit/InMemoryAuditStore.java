package com.poc.bankingagent.audit;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@ConditionalOnProperty(name = "app.audit.store", havingValue = "memory")
public class InMemoryAuditStore implements AuditEventStore {
    private static final int MAX_EVENTS = 500;

    private final ConcurrentLinkedDeque<AuditEvent> events = new ConcurrentLinkedDeque<>();

    @Override
    public void append(AuditEvent event) {
        events.addFirst(event);
        while (events.size() > MAX_EVENTS) {
            events.removeLast();
        }
    }

    @Override
    public List<AuditEvent> list(String target, AuditStatus status, String correlationId, int limit) {
        return events.stream()
                .filter(event -> target == null || event.target().equals(target))
                .filter(event -> status == null || event.status() == status)
                .filter(event -> correlationId == null || correlationId.equals(event.correlationId()))
                .sorted(Comparator.comparing(AuditEvent::timestamp).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public void clear() {
        events.clear();
    }
}
