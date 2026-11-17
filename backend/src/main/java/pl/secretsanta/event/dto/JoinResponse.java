package pl.secretsanta.event.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record JoinResponse(UUID participantId, UUID eventId, String name, BigDecimal budget) {
}
