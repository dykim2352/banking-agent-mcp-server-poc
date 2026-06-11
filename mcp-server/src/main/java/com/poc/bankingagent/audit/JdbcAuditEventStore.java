package com.poc.bankingagent.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "app.audit.store", havingValue = "jdbc", matchIfMissing = true)
public class JdbcAuditEventStore implements AuditEventStore {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAuditEventStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void append(AuditEvent event) {
        jdbcTemplate.update("""
                        insert into audit_events (
                            id, correlation_id, user_id, role, action_type, target, status,
                            error_code, message, created_at, elapsed_millis
                        )
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                event.id(),
                event.correlationId(),
                event.userId(),
                event.role(),
                event.actionType(),
                event.target(),
                event.status().name(),
                event.errorCode(),
                event.message(),
                Timestamp.from(event.timestamp()),
                event.elapsedMillis()
        );
    }

    @Override
    public List<AuditEvent> list(String target, AuditStatus status, String correlationId, int limit) {
        StringBuilder sql = new StringBuilder("""
                select id, correlation_id, user_id, role, action_type, target, status,
                       error_code, message, created_at, elapsed_millis
                from audit_events
                where 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (target != null) {
            sql.append(" and target = ?");
            params.add(target);
        }
        if (status != null) {
            sql.append(" and status = ?");
            params.add(status.name());
        }
        if (correlationId != null) {
            sql.append(" and correlation_id = ?");
            params.add(correlationId);
        }

        sql.append(" order by created_at desc limit ?");
        params.add(limit);

        return jdbcTemplate.query(sql.toString(), this::mapRow, params.toArray());
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from audit_events");
    }

    private AuditEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AuditEvent(
                rs.getString("id"),
                rs.getString("correlation_id"),
                rs.getString("user_id"),
                rs.getString("role"),
                rs.getString("action_type"),
                rs.getString("target"),
                AuditStatus.valueOf(rs.getString("status")),
                rs.getString("error_code"),
                rs.getString("message"),
                toInstant(rs.getTimestamp("created_at")),
                rs.getLong("elapsed_millis")
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
