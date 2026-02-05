-- Increase api_key column length to accommodate encrypted values
ALTER TABLE bots ALTER COLUMN api_key TYPE VARCHAR(500);
