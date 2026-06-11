package com.poc.bankingagent.job;

import com.poc.bankingagent.kafka.KafkaDeadLetterStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class AgentJobRequestedConsumer {
    private static final Logger log = LoggerFactory.getLogger(AgentJobRequestedConsumer.class);

    private final ObjectMapper objectMapper;
    private final AgentJobWorker worker;
    private final KafkaDeadLetterStore deadLetterStore;
    private final int maxAttempts;

    public AgentJobRequestedConsumer(ObjectMapper objectMapper,
                                     AgentJobWorker worker,
                                     KafkaDeadLetterStore deadLetterStore,
                                     @Value("${app.kafka.consumer.max-attempts:3}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.worker = worker;
        this.deadLetterStore = deadLetterStore;
        this.maxAttempts = maxAttempts;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.jobRequested:agent.job.requested}",
            groupId = "${app.kafka.consumer.job-group-id:banking-agent-job-consumer}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        Exception lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                AgentJobRequestedEvent event = objectMapper.readValue(record.value(), AgentJobRequestedEvent.class);
                worker.process(event);
                return;
            } catch (Exception e) {
                lastError = e;
                log.warn("Async job consume attempt failed. topic={}, key={}, attempt={}/{}, error={}",
                        record.topic(), record.key(), attempt, maxAttempts, e.getMessage());
            }
        }
        deadLetterStore.append(record.topic(), record.key(), record.value(), errorMessage(lastError), maxAttempts);
    }

    private String errorMessage(Exception e) {
        if (e == null) {
            return "Unknown async job consumer error";
        }
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }
}
