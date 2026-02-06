-- Bot ratings (ELO per bot per game)
-- Extensible design: nullable leaderboard_id for future private leaderboards
-- For now, leaderboard_id is NULL (global leaderboard)
CREATE TABLE bot_ratings (
    id BIGSERIAL PRIMARY KEY,
    bot_id BIGINT NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
    game VARCHAR(50) NOT NULL,
    leaderboard_id BIGINT NULL, -- NULL = global leaderboard, non-null = private leaderboard
    elo_rating INTEGER NOT NULL DEFAULT 1200,
    matches_played INTEGER NOT NULL DEFAULT 0,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0,
    draws INTEGER NOT NULL DEFAULT 0,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(bot_id, game, leaderboard_id)
);

-- Indexes for global leaderboard queries
CREATE INDEX idx_bot_ratings_global_game_elo ON bot_ratings(game, elo_rating DESC) WHERE leaderboard_id IS NULL;
CREATE INDEX idx_bot_ratings_bot_game ON bot_ratings(bot_id, game) WHERE leaderboard_id IS NULL;

-- Rating history per bot
CREATE TABLE rating_history (
    id BIGSERIAL PRIMARY KEY,
    bot_rating_id BIGINT NOT NULL REFERENCES bot_ratings(id) ON DELETE CASCADE,
    match_id UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    old_rating INTEGER NOT NULL,
    new_rating INTEGER NOT NULL,
    rating_change INTEGER NOT NULL,
    opponent_rating INTEGER NOT NULL,
    opponent_bot_id BIGINT NOT NULL REFERENCES bots(id) ON DELETE CASCADE,
    match_result VARCHAR(10) NOT NULL, -- WIN, LOSS, DRAW
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rating_history_bot_rating ON rating_history(bot_rating_id, created DESC);
CREATE INDEX idx_rating_history_match ON rating_history(match_id);

-- Materialized view for user rankings (computed from bot ratings)
-- Only includes global leaderboard (leaderboard_id IS NULL)
CREATE MATERIALIZED VIEW user_rankings AS
SELECT
    u.id as user_id,
    br.game,
    MAX(br.elo_rating) as best_bot_elo,
    AVG(br.elo_rating)::INTEGER as avg_bot_elo,
    COUNT(DISTINCT br.bot_id) as total_bots,
    SUM(br.matches_played) as total_matches,
    SUM(br.wins) as total_wins,
    SUM(br.losses) as total_losses,
    SUM(br.draws) as total_draws,
    CASE
        WHEN SUM(br.matches_played) > 0
        THEN (SUM(br.wins)::FLOAT / SUM(br.matches_played))
        ELSE 0
    END as win_rate
FROM users u
INNER JOIN bots b ON b.user_id = u.id
INNER JOIN bot_ratings br ON br.bot_id = b.id
WHERE br.leaderboard_id IS NULL -- Global leaderboard only
GROUP BY u.id, br.game;

CREATE UNIQUE INDEX idx_user_rankings_user_game ON user_rankings(user_id, game);
CREATE INDEX idx_user_rankings_game_elo ON user_rankings(game, best_bot_elo DESC);
