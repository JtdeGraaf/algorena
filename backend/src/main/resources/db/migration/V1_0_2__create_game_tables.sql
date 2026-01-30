CREATE TABLE matches
(
    id           UUID PRIMARY KEY,
    game         VARCHAR(50) NOT NULL,
    status       VARCHAR(50) NOT NULL,
    started_at   TIMESTAMP,
    finished_at  TIMESTAMP,
    created      TIMESTAMP   NOT NULL,
    last_updated TIMESTAMP   NOT NULL
);

CREATE TABLE match_participants
(
    id           UUID PRIMARY KEY,
    match_id     UUID      NOT NULL REFERENCES matches (id),
    bot_id       BIGINT    NOT NULL REFERENCES bots (id),
    player_index INTEGER   NOT NULL,
    score        DOUBLE PRECISION,
    created      TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL
);

CREATE TABLE match_moves
(
    id            UUID PRIMARY KEY,
    match_id      UUID      NOT NULL REFERENCES matches (id),
    player_index  INTEGER   NOT NULL,
    move_notation TEXT      NOT NULL,
    created       TIMESTAMP NOT NULL,
    last_updated  TIMESTAMP NOT NULL
);

CREATE TABLE chess_match_moves
(
    id              UUID PRIMARY KEY REFERENCES match_moves (id),
    from_square     VARCHAR(2),
    to_square       VARCHAR(2),
    promotion_piece VARCHAR(10)
);

CREATE TABLE game_states
(
    id           UUID PRIMARY KEY,
    match_id     UUID      NOT NULL REFERENCES matches (id),
    created      TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL
);

CREATE TABLE chess_game_states
(
    id               UUID PRIMARY KEY REFERENCES game_states (id),
    fen              VARCHAR(255) NOT NULL,
    pgn              TEXT,
    half_move_clock  INTEGER,
    full_move_number INTEGER
);

CREATE INDEX idx_match_participants_match_id ON match_participants (match_id);
CREATE INDEX idx_match_moves_match_id ON match_moves (match_id);
CREATE INDEX idx_game_states_match_id ON game_states (match_id);
