-- Create bots table
CREATE TABLE IF NOT EXISTS bots
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    name         VARCHAR(50) NOT NULL,
    description  VARCHAR(500),
    game         VARCHAR(20) NOT NULL,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created      TIMESTAMP   NOT NULL,
    last_updated TIMESTAMP   NOT NULL,
    CONSTRAINT fk_bots_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

-- Create bot_api_keys table
CREATE TABLE IF NOT EXISTS bot_api_keys
(
    id           BIGSERIAL PRIMARY KEY,
    bot_id       BIGINT       NOT NULL,
    key_hash     VARCHAR(255) NOT NULL UNIQUE,
    key_prefix   VARCHAR(10)  NOT NULL,
    name         VARCHAR(50),
    last_used    TIMESTAMP,
    expires_at   TIMESTAMP,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created      TIMESTAMP    NOT NULL,
    last_updated TIMESTAMP    NOT NULL,
    CONSTRAINT fk_bot_api_keys_bot
        FOREIGN KEY (bot_id)
            REFERENCES bots (id)
            ON DELETE CASCADE
);
