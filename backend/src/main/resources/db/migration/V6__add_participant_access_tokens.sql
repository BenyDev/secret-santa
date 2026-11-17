CREATE TABLE secret_santa.participant_access_tokens (
    id UUID PRIMARY KEY,
    participant_id UUID UNIQUE NOT NULL REFERENCES secret_santa.participants (id),
    token_hash VARCHAR(64) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
