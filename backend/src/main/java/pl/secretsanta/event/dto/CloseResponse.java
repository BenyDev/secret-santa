package pl.secretsanta.event.dto;

import pl.secretsanta.event.GiftEvent;

import java.util.UUID;

public record CloseResponse(UUID id, GiftEvent.Status status) {
}
