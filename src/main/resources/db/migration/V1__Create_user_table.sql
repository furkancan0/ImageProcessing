CREATE TABLE roles
(
   id           BIGSERIAL PRIMARY KEY,
   description  VARCHAR(255) NOT NULL,
   name         VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE users
(
    id                        BIGSERIAL PRIMARY KEY,
    email                     VARCHAR(255) NOT NULL UNIQUE,
    password                  VARCHAR(255) NOT NULL UNIQUE,
    created_date              timestamp,
    role_id                   bigint not null references roles (id)
);