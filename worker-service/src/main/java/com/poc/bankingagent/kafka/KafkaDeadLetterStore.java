package com.poc.bankingagent.kafka;

public interface KafkaDeadLetterStore {
    void append(String topic, String messageKey, String payload, String errorMessage, int retryCount);

    void clear();
}
