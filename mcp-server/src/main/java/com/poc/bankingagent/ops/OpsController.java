package com.poc.bankingagent.ops;

import com.poc.bankingagent.common.NotFoundException;
import com.poc.bankingagent.job.AgentJob;
import com.poc.bankingagent.job.AgentJobStatus;
import com.poc.bankingagent.job.AgentJobStore;
import com.poc.bankingagent.kafka.KafkaDeadLetter;
import com.poc.bankingagent.kafka.KafkaDeadLetterStore;
import com.poc.bankingagent.security.AccessPolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ops")
public class OpsController {
    private final KafkaDeadLetterStore deadLetterStore;
    private final AgentJobStore jobStore;
    private final AccessPolicyService accessPolicyService;

    public OpsController(KafkaDeadLetterStore deadLetterStore, AgentJobStore jobStore, AccessPolicyService accessPolicyService) {
        this.deadLetterStore = deadLetterStore;
        this.jobStore = jobStore;
        this.accessPolicyService = accessPolicyService;
    }

    @GetMapping("/jobs")
    public Map<String, List<AgentJob>> jobs(
            @RequestParam(required = false) AgentJobStatus status,
            @RequestParam(defaultValue = "100") int limit
    ) {
        accessPolicyService.assertAdmin("jobs");
        int safeLimit = safeLimit(limit);
        return Map.of("jobs", jobStore.list(status, safeLimit));
    }

    @GetMapping("/jobs/{jobId}")
    public AgentJob job(@PathVariable String jobId) {
        accessPolicyService.assertAdmin("job " + jobId);
        return jobStore.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Async job not found: " + jobId));
    }

    @GetMapping("/dead-letters")
    public Map<String, List<KafkaDeadLetter>> deadLetters(
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "100") int limit
    ) {
        accessPolicyService.assertAdmin("dead letters");
        return Map.of("deadLetters", deadLetterStore.list(blankToNull(topic), safeLimit(limit)));
    }

    private int safeLimit(int limit) {
        return Math.max(1, Math.min(limit, 500));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
