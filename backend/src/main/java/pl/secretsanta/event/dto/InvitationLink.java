package pl.secretsanta.event.dto;

import java.util.UUID;

public record InvitationLink(UUID eventId, String token) {
    @Override
    public String toString() {
        return "InvitationLink[eventId=" + eventId + ", token=redacted]";
    }
}
