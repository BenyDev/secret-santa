package pl.secretsanta.event.draw;

import java.util.UUID;

public record DrawPair(UUID giverId, UUID recipientId) {
}
