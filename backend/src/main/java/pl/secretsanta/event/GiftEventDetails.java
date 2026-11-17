package pl.secretsanta.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GiftEventDetails(UUID id, String name, BigDecimal budget, GiftEvent.Status status,
                               Instant createdAt, UUID organizerId, List<ParticipantDetails> participants) {
    public record ParticipantDetails(UUID id, String nickname) {
    }
}
