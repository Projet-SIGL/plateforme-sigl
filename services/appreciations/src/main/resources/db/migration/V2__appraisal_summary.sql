CREATE TABLE appraisal_summary (
    id            UUID PRIMARY KEY,
    apprentice_id UUID        NOT NULL,
    period        VARCHAR(20) NOT NULL,
    generated_by  UUID        NOT NULL,
    status        VARCHAR(20) NOT NULL
);

CREATE TABLE appraisal_summary_item (
    summary_id   UUID NOT NULL REFERENCES appraisal_summary (id),
    appraisal_id UUID NOT NULL REFERENCES appraisal (id),
    PRIMARY KEY (summary_id, appraisal_id)
);
