CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name varchar(255) NOT NULL,
    email varchar(512) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email),
    CONSTRAINT valid_email CHECK (email LIKE '%@%'),
    CONSTRAINT no_empty_user_name CHECK (name NOT LIKE ' ' AND name NOT LIKE '')
    );

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    description VARCHAR(512) NOT NULL,
    requester_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT no_empty_request_description CHECK (description NOT LIKE ' ' AND description NOT LIKE ''),
    CONSTRAINT fk_request_user FOREIGN KEY(requester_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(512) NOT NULL,
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,
    CONSTRAINT no_empty_item_name CHECK (name NOT LIKE ' ' AND name NOT LIKE ''),
    CONSTRAINT no_empty_item_description CHECK (description NOT LIKE ' ' AND description NOT LIKE ''),
    CONSTRAINT fk_item_user FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_request FOREIGN KEY(request_id) REFERENCES requests(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date TIMESTAMP WITHOUT TIME ZONE,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL,
    status VARCHAR(64) NOT NULL,
    CONSTRAINT valid_status CHECK (status IN('WAITING', 'APPROVED', 'REJECTED', 'CANCELED')),
    CONSTRAINT fk_booking_item FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_user FOREIGN KEY(booker_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    text VARCHAR(2000),
    item_id BIGINT NOT NULL,
    author_id BIGINT,
    created  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_comment_item FOREIGN KEY(item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY(author_id) REFERENCES users(id) ON DELETE SET NULL
    );