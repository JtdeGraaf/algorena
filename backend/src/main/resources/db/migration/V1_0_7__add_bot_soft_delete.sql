-- Add soft delete support to bots table
ALTER TABLE bots ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
