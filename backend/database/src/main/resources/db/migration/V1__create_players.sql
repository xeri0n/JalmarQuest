CREATE TABLE IF NOT EXISTS players (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    choice_log JSONB NOT NULL DEFAULT '[]'::jsonb,
    quest_log JSONB NOT NULL DEFAULT '[]'::jsonb,
    status_effects JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_players_updated_at ON players;
CREATE TRIGGER trg_set_players_updated_at
BEFORE UPDATE ON players
FOR EACH ROW EXECUTE PROCEDURE set_updated_at();
