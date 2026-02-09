CREATE TABLE connect4_game_states (
    id BIGINT NOT NULL,
    board VARCHAR(42) NOT NULL,
    last_move_column INTEGER,
    CONSTRAINT pk_connect4_game_states PRIMARY KEY (id),
    CONSTRAINT fk_connect4_game_states_parent FOREIGN KEY (id) REFERENCES game_states (id)
);

CREATE TABLE connect4_match_moves (
    id BIGINT NOT NULL,
    column_index INTEGER NOT NULL,
    CONSTRAINT pk_connect4_match_moves PRIMARY KEY (id),
    CONSTRAINT fk_connect4_match_moves_parent FOREIGN KEY (id) REFERENCES match_moves (id)
);
