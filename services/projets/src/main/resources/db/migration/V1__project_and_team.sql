CREATE TABLE project (
    id         UUID PRIMARY KEY,
    manager_id UUID         NOT NULL,
    name       VARCHAR(200) NOT NULL,
    status     VARCHAR(20)  NOT NULL
);

CREATE TABLE team (
    id         UUID PRIMARY KEY,
    project_id UUID         NOT NULL REFERENCES project (id),
    name       VARCHAR(100) NOT NULL,
    UNIQUE (project_id, name)
);

CREATE TABLE team_member (
    team_id UUID NOT NULL REFERENCES team (id),
    user_id UUID NOT NULL,
    PRIMARY KEY (team_id, user_id)
);
