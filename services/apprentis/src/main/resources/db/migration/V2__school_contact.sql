CREATE TABLE school_contact (
    id        UUID PRIMARY KEY,
    school_id UUID         NOT NULL REFERENCES school (id),
    name      VARCHAR(200) NOT NULL,
    email     VARCHAR(200) NOT NULL,
    phone     VARCHAR(50)
);
