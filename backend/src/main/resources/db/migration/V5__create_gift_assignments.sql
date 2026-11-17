ALTER TABLE secret_santa.gift_events
    DROP CONSTRAINT gift_events_status_check,
    ADD CONSTRAINT gift_events_status_check CHECK (status IN ('OPEN', 'CLOSED', 'DRAWN'));

-- Composite keys ensure both participants belong to the assignment's event.
ALTER TABLE secret_santa.participants
    ADD CONSTRAINT uk_participants_event_id UNIQUE (event_id, id);

CREATE TABLE secret_santa.gift_assignments (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES secret_santa.gift_events (id),
    giver_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    CONSTRAINT ck_gift_assignments_not_self CHECK (giver_id <> recipient_id),
    CONSTRAINT uk_gift_assignments_giver UNIQUE (event_id, giver_id),
    CONSTRAINT uk_gift_assignments_recipient UNIQUE (event_id, recipient_id),
    CONSTRAINT fk_gift_assignments_giver FOREIGN KEY (event_id, giver_id)
        REFERENCES secret_santa.participants (event_id, id),
    CONSTRAINT fk_gift_assignments_recipient FOREIGN KEY (event_id, recipient_id)
        REFERENCES secret_santa.participants (event_id, id)
);
