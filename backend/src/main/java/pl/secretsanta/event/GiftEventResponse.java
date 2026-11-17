package pl.secretsanta.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GiftEventResponse(UUID id, String name, BigDecimal budget, GiftEvent.Status status, Instant createdAt,
                                UUID organizerId, OrganizerParticipant organizerParticipant) {
    public record OrganizerParticipant(UUID id, UUID userId, String nickname, Instant createdAt) {
    }
}
