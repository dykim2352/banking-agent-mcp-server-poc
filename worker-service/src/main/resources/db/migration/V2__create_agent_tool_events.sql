create table if not exists agent_tool_events (
    event_id varchar(36) primary key,
    correlation_id varchar(100),
    event_type varchar(50) not null,
    tool_name varchar(100) not null,
    status varchar(50) not null,
    error_code varchar(100),
    message varchar(1000),
    occurred_at timestamp not null,
    consumed_at timestamp not null
);

create index if not exists idx_agent_tool_events_correlation_id on agent_tool_events (correlation_id);
create index if not exists idx_agent_tool_events_tool_status on agent_tool_events (tool_name, status);
create index if not exists idx_agent_tool_events_occurred_at on agent_tool_events (occurred_at desc);
