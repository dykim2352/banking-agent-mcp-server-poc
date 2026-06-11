package com.poc.bankingagent.kafka;

import java.time.Instant;

public record KafkaDeadLetter(
        String id,
        String topic,
        String messageKey,
        String payload,
        String errorMessage,
        int retryCount,
        Instant createdAt
) {
}
