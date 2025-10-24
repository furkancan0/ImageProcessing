CREATE TABLE images
(
    id                        BIGSERIAL PRIMARY KEY,
    name                      VARCHAR(255),
    type                      VARCHAR(255),
    file_size                 bigint NOT NULL,
    deleted                   BOOLEAN NOT NULL DEFAULT FALSE,
    image_data                BYTEA,
    image_date                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id                   bigint not null references users (id)
);