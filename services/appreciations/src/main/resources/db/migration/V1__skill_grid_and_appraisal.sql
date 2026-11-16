CREATE TABLE criterion (
    id        UUID PRIMARY KEY,
    label     VARCHAR(200) NOT NULL,
    max_score INTEGER      NOT NULL CHECK (max_score > 0),
    active    BOOLEAN      NOT NULL
);

CREATE TABLE appraisal (
    id            UUID PRIMARY KEY,
    apprentice_id UUID        NOT NULL,
    project_id    UUID        NOT NULL,
    evaluator_id  UUID        NOT NULL,
    period        VARCHAR(20) NOT NULL,
    status        VARCHAR(20) NOT NULL
);

CREATE TABLE appraisal_score (
    appraisal_id UUID    NOT NULL REFERENCES appraisal (id),
    criterion_id UUID    NOT NULL,
    score        INTEGER NOT NULL CHECK (score >= 0),
    PRIMARY KEY (appraisal_id, criterion_id)
);

-- Effective supervision (rule R3): who really supervised which apprentice on which project.
-- Fed from the AssignmentDecided event; consumption is out of scope of this migration.
CREATE TABLE effective_supervision (
    evaluator_id  UUID NOT NULL,
    apprentice_id UUID NOT NULL,
    project_id    UUID NOT NULL,
    PRIMARY KEY (evaluator_id, apprentice_id, project_id)
);
