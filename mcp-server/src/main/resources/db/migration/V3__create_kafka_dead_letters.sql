create table if not exists kafka_dead_letters (
    id varchar(36) primary key,
    topic varchar(255) not null,
    message_key varchar(255),
    payload varchar(4000) not null,
    error_message varchar(1000) not null,
    retry_count integer not null,
    created_at timestamp not null
);

create index if not exists idx_kafka_dead_letters_created_at on kafka_dead_letters (created_at desc);
create index if not exists idx_kafka_dead_letters_topic on kafka_dead_letters (topic);
