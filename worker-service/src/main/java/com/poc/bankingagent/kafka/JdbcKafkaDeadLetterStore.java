package com.poc.bankingagent.kafka;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcKafkaDeadLetterStore implements KafkaDeadLetterStore {
    private final JdbcTemplate jdbcTemplate;

    public JdbcKafkaDeadLetterStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void append(String topic, String messageKey, String payload, String errorMessage, int retryCount) {
        jdbcTemplate.update("""
                        insert into kafka_dead_letters (
                            id, topic, message_key, payload, error_message, retry_count, created_at
                        )
                        values (?, ?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID().toString(),
                topic,
                messageKey,
                payload,
                errorMessage,
                retryCount,
                Timestamp.from(Instant.now())
        );
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from kafka_dead_letters");
    }
}
