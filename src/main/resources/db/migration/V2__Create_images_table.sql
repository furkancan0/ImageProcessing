CREATE TABLE images
(
    id                        BIGSERIAL PRIMARY KEY,
    name                      VARCHAR(255),
    type                      VARCHAR(255),
    description               VARCHAR(255),
    file_size                 bigint NOT NULL,
    image_data                BYTEA,
    thumbnail                 BYTEA,
    search_vector             tsvector,
    image_date                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id                   bigint not null references users (id)
);