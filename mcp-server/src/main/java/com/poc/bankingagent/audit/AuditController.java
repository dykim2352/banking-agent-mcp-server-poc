package com.poc.bankingagent.audit;

import com.poc.bankingagent.security.AccessPolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    private final AuditEventStore store;
    private final AccessPolicyService accessPolicyService;

    public AuditController(AuditEventStore store, AccessPolicyService accessPolicyService) {
        this.store = store;
        this.accessPolicyService = accessPolicyService;
    }

    @GetMapping("/events")
    public Map<String, List<AuditEvent>> events(
            @RequestParam(required = false) String target,
            @RequestParam(required = false) AuditStatus status,
            @RequestParam(required = false) String correlationId,
            @RequestParam(defaultValue = "100") int limit
    ) {
        accessPolicyService.assertAdmin("audit events");
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return Map.of("events", store.list(blankToNull(target), status, blankToNull(correlationId), safeLimit));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
