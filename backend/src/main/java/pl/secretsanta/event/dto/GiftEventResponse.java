package pl.secretsanta.event.dto;

import pl.secretsanta.event.GiftEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GiftEventResponse(UUID id, String name, BigDecimal budget, GiftEvent.Status status, Instant createdAt,
                                UUID organizerId, OrganizerParticipant organizerParticipant) {
}

