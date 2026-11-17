-- Minimal task table: the closure guard of a project needs to know whether work is still open.
-- The full task model belongs to the task stories.
CREATE TABLE task (
    id         UUID PRIMARY KEY,
    project_id UUID        NOT NULL REFERENCES project (id),
    status     VARCHAR(20) NOT NULL
);
