create table if not exists agent_jobs (
    job_id varchar(36) primary key,
    tool_name varchar(100) not null,
    status varchar(20) not null,
    request_payload varchar(4000) not null,
    result_payload varchar(4000),
    error_code varchar(100),
    error_message varchar(1000),
    created_at timestamp not null,
    updated_at timestamp not null
);

create index if not exists idx_agent_jobs_status on agent_jobs (status);
create index if not exists idx_agent_jobs_updated_at on agent_jobs (updated_at desc);
