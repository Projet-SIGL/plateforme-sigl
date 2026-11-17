CREATE TABLE school (
    id   UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE apprentice (
    id        UUID PRIMARY KEY,
    name      VARCHAR(200) NOT NULL,
    school_id UUID         NOT NULL REFERENCES school (id),
    master_id UUID,
    archived  BOOLEAN      NOT NULL
);
