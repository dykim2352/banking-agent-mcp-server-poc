create table if not exists audit_events (
    id varchar(36) primary key,
    correlation_id varchar(100),
    user_id varchar(100) not null,
    role varchar(50) not null,
    action_type varchar(50) not null,
    target varchar(255) not null,
    status varchar(50) not null,
    error_code varchar(100),
    message varchar(1000),
    created_at timestamp not null,
    elapsed_millis bigint not null
);

create index if not exists idx_audit_events_created_at on audit_events (created_at desc);
create index if not exists idx_audit_events_target_status on audit_events (target, status);
create index if not exists idx_audit_events_correlation_id on audit_events (correlation_id);
