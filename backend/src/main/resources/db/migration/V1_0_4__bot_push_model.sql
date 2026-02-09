-- Add endpoint and api_key columns to bots table
ALTER TABLE bots ADD COLUMN endpoint VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE bots ADD COLUMN api_key VARCHAR(255);

-- Remove default after adding (PostgreSQL requires default for NOT NULL on existing rows)
ALTER TABLE bots ALTER COLUMN endpoint DROP DEFAULT;

-- Drop bot_api_keys table (no longer needed - switching to push model)
DROP TABLE IF EXISTS bot_api_keys;
