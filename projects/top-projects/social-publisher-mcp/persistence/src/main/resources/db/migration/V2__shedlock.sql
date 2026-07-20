-- ShedLock table: guarantees the scheduled-publish poller runs on only one node at a time.
create table shedlock (
    name       varchar(64)  not null primary key,
    lock_until timestamptz  not null,
    locked_at  timestamptz  not null,
    locked_by  varchar(255) not null
);
