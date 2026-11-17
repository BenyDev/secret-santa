package pl.secretsanta.event.dto;

import pl.secretsanta.event.GiftEvent;

import java.util.UUID;

public record DrawResponse(UUID id, GiftEvent.Status status) {
}
