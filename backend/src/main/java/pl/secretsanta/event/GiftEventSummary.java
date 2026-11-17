package pl.secretsanta.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GiftEventSummary(UUID id, String name, BigDecimal budget, GiftEvent.Status status,
                               long participantCount, Instant createdAt) {
}
