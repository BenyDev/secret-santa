package pl.secretsanta.event.dto;

import pl.secretsanta.event.GiftEvent;

import java.math.BigDecimal;
import java.util.UUID;

public record ParticipantMeResponse(UUID participantId, UUID eventId, String nickname, String name,
                                    BigDecimal budget, GiftEvent.Status status) {
}
