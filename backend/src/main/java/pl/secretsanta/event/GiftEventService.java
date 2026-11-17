package pl.secretsanta.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;
import pl.secretsanta.event.dto.CloseResponse;
import pl.secretsanta.event.dto.CreateGiftEventRequest;
import pl.secretsanta.event.dto.GiftEventDetails;
import pl.secretsanta.event.dto.GiftEventResponse;
import pl.secretsanta.event.dto.GiftEventSummary;
import pl.secretsanta.event.dto.OrganizerParticipant;
import pl.secretsanta.event.dto.ParticipantDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class GiftEventService {
    private final AppUserRepository users;
    private final GiftEventRepository events;
    private final ParticipantRepository participants;

    public GiftEventService(AppUserRepository users, GiftEventRepository events, ParticipantRepository participants) {
        this.users = users;
        this.events = events;
        this.participants = participants;
    }

    @Transactional
    public GiftEventResponse create(UUID organizerId, CreateGiftEventRequest request) {
        AppUser organizer = users.findById(organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        GiftEvent event = events.save(new GiftEvent(request.name(), request.budget().setScale(2), organizer, createdAt));
        Participant participant = participants.save(new Participant(event, organizer, request.organizerNickname(), createdAt));

        return new GiftEventResponse(event.getId(), event.getName(), event.getBudget(), event.getStatus(),
                event.getCreatedAt(), event.getOrganizer().getId(),
                new OrganizerParticipant(participant.getId(), organizer.getId(),
                        participant.getNickname(), participant.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public List<GiftEventSummary> list(UUID organizerId) {
        return events.findSummariesByOrganizerId(organizerId);
    }

    @Transactional(readOnly = true)
    public GiftEventDetails details(UUID organizerId, UUID eventId) {
        GiftEvent event = events.findByIdAndOrganizerId(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var participantDetails = participants.findByEventIdOrderByCreatedAtAscIdAsc(event.getId()).stream()
                .map(participant -> new ParticipantDetails(participant.getId(), participant.getNickname()))
                .toList();
        return new GiftEventDetails(event.getId(), event.getName(), event.getBudget(), event.getStatus(),
                event.getCreatedAt(), organizerId, participantDetails);
    }

    @Transactional
    public CloseResponse close(UUID organizerId, UUID eventId) {
        // Join and invitation rotation lock the same event row before reading its status.
        GiftEvent event = events.lockOwnedEvent(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono wydarzenia."));
        if (event.getStatus() != GiftEvent.Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lista uczestników jest już zamknięta.");
        }
        if (participants.countByEventId(eventId) < 3) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wydarzenie wymaga co najmniej 3 uczestników.");
        }
        event.close();
        return new CloseResponse(event.getId(), event.getStatus());
    }
}
