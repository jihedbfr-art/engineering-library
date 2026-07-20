-- Core publishing schema.
-- overrides_json keeps the per-platform caption/hashtag/title overrides so a scheduled
-- publication can be replayed faithfully at its due time; it is null when no overrides were given.

create table publication (
    id            uuid primary key,
    caption       text,
    status        varchar(32)  not null,
    scheduled_at  timestamptz,
    overrides_json text,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now()
);

create table publication_media (
    id             uuid primary key,
    publication_id uuid not null references publication (id) on delete cascade,
    source_url     text,
    object_key     text,
    media_type     varchar(16),
    mime           varchar(128),
    sha256         varchar(64),
    size_bytes     bigint,
    duration_ms    bigint
);

create table publication_target (
    id               uuid primary key,
    publication_id   uuid not null references publication (id) on delete cascade,
    platform         varchar(32) not null,
    status           varchar(32) not null,
    external_post_id varchar(255),
    permalink        varchar(1024),
    error_code       varchar(128),
    error_message    text
);

create table publication_attempt (
    id               uuid primary key,
    target_id        uuid not null references publication_target (id) on delete cascade,
    attempt_no       int  not null,
    started_at       timestamptz,
    finished_at      timestamptz,
    http_status      int,
    response_excerpt text
);

create table platform_credential (
    id                uuid primary key,
    platform          varchar(32) not null unique,
    payload_encrypted bytea       not null,
    expires_at        timestamptz,
    updated_at        timestamptz not null default now()
);

create index idx_publication_status on publication (status);
create index idx_publication_scheduled on publication (scheduled_at) where status = 'SCHEDULED';
create index idx_target_publication on publication_target (publication_id);
create index idx_attempt_target on publication_attempt (target_id);
