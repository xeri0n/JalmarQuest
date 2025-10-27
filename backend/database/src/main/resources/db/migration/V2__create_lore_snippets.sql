CREATE TABLE IF NOT EXISTS lore_snippets (
    id TEXT PRIMARY KEY,
    event_text TEXT NOT NULL,
    choice_options JSONB NOT NULL,
    consequences JSONB NOT NULL,
    conditions JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
