package pl.secretsanta.event.dto;

import java.time.Instant;
import java.util.UUID;

public record OrganizerParticipant(UUID id, UUID userId, String nickname, Instant createdAt) {
}
