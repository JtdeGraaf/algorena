CREATE TABLE IF NOT EXISTS users
(
    id           BIGSERIAL PRIMARY KEY,
    provider     TEXT,
    provider_id  TEXT,
    email        TEXT UNIQUE,
    username     TEXT UNIQUE             NOT NULL,
    name         TEXT,
    language     VARCHAR(2) default 'EN' not null,
    image_url    TEXT,
    created      TIMESTAMP               NOT NULL,
    last_updated TIMESTAMP               NOT NULL,
    UNIQUE (provider, provider_id)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);