CREATE TABLE calendar_period (
    id            UUID PRIMARY KEY,
    apprentice_id UUID        NOT NULL REFERENCES apprentice (id),
    type          VARCHAR(20) NOT NULL,
    start_date    DATE        NOT NULL,
    end_date      DATE        NOT NULL,
    CHECK (start_date <= end_date)
);
CREATE INDEX idx_calendar_period_apprentice ON calendar_period (apprentice_id);
