package com.poc.bankingagent.kafka;

import java.util.List;

public interface KafkaDeadLetterStore {
    void append(String topic, String messageKey, String payload, String errorMessage, int retryCount);

    List<KafkaDeadLetter> list(String topic, int limit);

    void clear();
}
