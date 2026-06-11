package com.poc.bankingagent.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AgentJobPublisher {
    private static final Logger log = LoggerFactory.getLogger(AgentJobPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String jobRequestedTopic;

    public AgentJobPublisher(KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper,
                             @Value("${app.kafka.enabled:true}") boolean enabled,
                             @Value("${app.kafka.topics.jobRequested:agent.job.requested}") String jobRequestedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.jobRequestedTopic = jobRequestedTopic;
    }

    public void publishJobRequested(String correlationId, String jobId, String toolName, JsonNode arguments) {
        AgentJobRequestedEvent event = new AgentJobRequestedEvent(
                jobId,
                correlationId,
                toolName,
                arguments,
                Instant.now()
        );
        String payload = toJson(event);
        if (!enabled) {
            log.debug("Kafka disabled. topic={}, key={}, payload={}", jobRequestedTopic, jobId, payload);
            return;
        }
        try {
            kafkaTemplate.send(jobRequestedTopic, jobId, payload);
        } catch (Exception e) {
            log.warn("Failed to publish async job request. topic={}, jobId={}, error={}", jobRequestedTopic, jobId, e.getMessage());
        }
    }

    private String toJson(AgentJobRequestedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.warn("Failed to serialize async job request. jobId={}, toolName={}, error={}", event.jobId(), event.toolName(), e.getMessage());
            return "{}";
        }
    }
}
