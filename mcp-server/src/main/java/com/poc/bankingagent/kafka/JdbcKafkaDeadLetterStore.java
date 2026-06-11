package com.poc.bankingagent.kafka;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    public List<KafkaDeadLetter> list(String topic, int limit) {
        StringBuilder sql = new StringBuilder("""
                select id, topic, message_key, payload, error_message, retry_count, created_at
                from kafka_dead_letters
                where 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (topic != null) {
            sql.append(" and topic = ?");
            params.add(topic);
        }

        sql.append(" order by created_at desc limit ?");
        params.add(limit);

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from kafka_dead_letters");
    }

    private KafkaDeadLetter mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new KafkaDeadLetter(
                rs.getString("id"),
                rs.getString("topic"),
                rs.getString("message_key"),
                rs.getString("payload"),
                rs.getString("error_message"),
                rs.getInt("retry_count"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
