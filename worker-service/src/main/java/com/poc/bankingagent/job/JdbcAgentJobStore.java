package com.poc.bankingagent.job;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcAgentJobStore implements AgentJobStore {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAgentJobStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AgentJob create(String toolName, String requestPayload) {
        Instant now = Instant.now();
        AgentJob job = new AgentJob(
                UUID.randomUUID().toString(),
                toolName,
                AgentJobStatus.PENDING,
                requestPayload,
                null,
                null,
                null,
                now,
                now
        );
        jdbcTemplate.update("""
                        insert into agent_jobs (
                            job_id, tool_name, status, request_payload, result_payload,
                            error_code, error_message, created_at, updated_at
                        )
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                job.jobId(),
                job.toolName(),
                job.status().name(),
                job.requestPayload(),
                job.resultPayload(),
                job.errorCode(),
                job.errorMessage(),
                Timestamp.from(job.createdAt()),
                Timestamp.from(job.updatedAt())
        );
        return job;
    }

    @Override
    public void markRunning(String jobId) {
        int updated = jdbcTemplate.update("""
                        update agent_jobs
                        set status = ?, updated_at = ?
                        where job_id = ?
                        """,
                AgentJobStatus.RUNNING.name(),
                Timestamp.from(Instant.now()),
                jobId
        );
        assertUpdated(jobId, updated);
    }

    @Override
    public void markCompleted(String jobId, String resultPayload) {
        int updated = jdbcTemplate.update("""
                        update agent_jobs
                        set status = ?, result_payload = ?, error_code = null, error_message = null, updated_at = ?
                        where job_id = ?
                        """,
                AgentJobStatus.COMPLETED.name(),
                resultPayload,
                Timestamp.from(Instant.now()),
                jobId
        );
        assertUpdated(jobId, updated);
    }

    @Override
    public void markFailed(String jobId, String errorCode, String errorMessage) {
        int updated = jdbcTemplate.update("""
                        update agent_jobs
                        set status = ?, error_code = ?, error_message = ?, updated_at = ?
                        where job_id = ?
                        """,
                AgentJobStatus.FAILED.name(),
                errorCode,
                errorMessage,
                Timestamp.from(Instant.now()),
                jobId
        );
        assertUpdated(jobId, updated);
    }

    @Override
    public Optional<AgentJob> findById(String jobId) {
        List<AgentJob> jobs = jdbcTemplate.query("""
                        select job_id, tool_name, status, request_payload, result_payload,
                               error_code, error_message, created_at, updated_at
                        from agent_jobs
                        where job_id = ?
                        """,
                this::mapRow,
                jobId
        );
        return jobs.stream().findFirst();
    }

    @Override
    public void clear() {
        jdbcTemplate.update("delete from agent_jobs");
    }

    private AgentJob mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AgentJob(
                rs.getString("job_id"),
                rs.getString("tool_name"),
                AgentJobStatus.valueOf(rs.getString("status")),
                rs.getString("request_payload"),
                rs.getString("result_payload"),
                rs.getString("error_code"),
                rs.getString("error_message"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private void assertUpdated(String jobId, int updated) {
        if (updated == 0) {
            throw new UnknownAgentJobException(jobId);
        }
    }
}
