CREATE TABLE IF NOT EXISTS descriptions (
id          serial       primary key,
created_at  timestamp    not null default now(),
deleted_at  timestamp,
updated_at  timestamp    not null default now(),
hn_story_id integer      REFERENCES hn_stories (id),
body        varchar(100) not null check (body <> '')
);
