package pl.secretsanta.event.dto;

import java.util.UUID;

public record ParticipantDrawResponse(UUID recipientId, String recipientNickname, String wishlist) {
}
