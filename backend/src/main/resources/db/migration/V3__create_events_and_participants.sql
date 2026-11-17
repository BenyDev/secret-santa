CREATE TABLE secret_santa.gift_events (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL CHECK (btrim(name) <> ''),
    budget NUMERIC(10, 2) NOT NULL CHECK (budget > 0),
    organizer_id UUID NOT NULL REFERENCES secret_santa.app_users (id),
    status VARCHAR(16) NOT NULL CHECK (status IN ('OPEN')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_gift_events_organizer ON secret_santa.gift_events (organizer_id);

CREATE TABLE secret_santa.participants (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES secret_santa.gift_events (id),
    user_id UUID REFERENCES secret_santa.app_users (id),
    nickname VARCHAR(60) NOT NULL CHECK (btrim(nickname) <> ''),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_participants_event_user UNIQUE (event_id, user_id)
);

-- At commit, the organizer must also be a participant in their own event.
ALTER TABLE secret_santa.gift_events
    ADD CONSTRAINT fk_event_organizer_participant
    FOREIGN KEY (id, organizer_id) REFERENCES secret_santa.participants (event_id, user_id)
    DEFERRABLE INITIALLY DEFERRED;
