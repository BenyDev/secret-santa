ALTER TABLE secret_santa.gift_events
    ADD COLUMN invitation_token_hash VARCHAR(64),
    ADD CONSTRAINT uk_gift_events_invitation_token_hash UNIQUE (invitation_token_hash),
    DROP CONSTRAINT gift_events_status_check,
    ADD CONSTRAINT gift_events_status_check CHECK (status IN ('OPEN', 'CLOSED'));

CREATE UNIQUE INDEX uk_participants_event_nickname_ci
    ON secret_santa.participants (event_id, lower(nickname));
