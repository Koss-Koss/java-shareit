CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name varchar(255) NOT NULL,
    email varchar(512) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email),
    CONSTRAINT valid_email CHECK (email LIKE '%@%'),
    CONSTRAINT no_empty_name CHECK (name NOT LIKE ' ')
    );