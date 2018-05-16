CREATE TABLE IF NOT EXISTS hn_stories (
id serial    primary key,
created_at   timestamp not null default now(),
deleted_at   timestamp,
updated_at   timestamp not null default now(),
hn_id        integer not null,
url          varchar(255)
);

CREATE INDEX ON hn_stories (hn_id);
