package pl.secretsanta.event.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record JoinResponse(UUID participantId, UUID eventId, String name, BigDecimal budget,
                           String nickname, String participantAccessToken) {
    @Override
    public String toString() {
        return "JoinResponse[participantId=" + participantId + ", eventId=" + eventId + "]";
    }
}
