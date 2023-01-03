CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name varchar(255) NOT NULL,
    email varchar(512) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email),
    CONSTRAINT valid_email CHECK (email LIKE '%@%'),
    CONSTRAINT no_empty_name CHECK (name NOT LIKE ' ' AND name NOT LIKE '')
    );

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(512) NOT NULL,
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,
    CONSTRAINT no_empty_name CHECK (name NOT LIKE ' ' AND name NOT LIKE ''),
    CONSTRAINT no_empty_description CHECK (description NOT LIKE ' ' AND description NOT LIKE ''),
    CONSTRAINT fk_item_user FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
    );