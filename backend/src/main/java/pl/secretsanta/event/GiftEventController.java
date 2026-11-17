package pl.secretsanta.event;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import pl.secretsanta.event.dto.CloseResponse;
import pl.secretsanta.event.dto.CreateGiftEventRequest;
import pl.secretsanta.event.dto.GiftEventDetails;
import pl.secretsanta.event.dto.GiftEventResponse;
import pl.secretsanta.event.dto.GiftEventSummary;
import pl.secretsanta.event.dto.DrawResponse;

@RestController
public class GiftEventController {
    private final GiftEventService events;

    public GiftEventController(GiftEventService events) {
        this.events = events;
    }

    @PostMapping("/api/events")
    @ResponseStatus(HttpStatus.CREATED)
    public GiftEventResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateGiftEventRequest request) {
        return events.create(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/api/events")
    public List<GiftEventSummary> list(@AuthenticationPrincipal Jwt jwt) {
        return events.list(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/api/events/{eventId}")
    public GiftEventDetails details(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return events.details(UUID.fromString(jwt.getSubject()), eventId);
    }

    @PostMapping("/api/events/{eventId}/close")
    public CloseResponse close(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return events.close(UUID.fromString(jwt.getSubject()), eventId);
    }

    @PostMapping("/api/events/{eventId}/draw")
    public DrawResponse draw(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return events.draw(UUID.fromString(jwt.getSubject()), eventId);
    }

}
