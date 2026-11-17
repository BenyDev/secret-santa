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
import pl.secretsanta.event.dto.DrawResponse;
import pl.secretsanta.event.draw.SecretSantaDraw;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GiftEventService {
    private final AppUserRepository users;
    private final GiftEventRepository events;
    private final ParticipantRepository participants;
    private final GiftAssignmentRepository assignments;
    private final SecretSantaDraw drawAlgorithm = new SecretSantaDraw(new SecureRandom());

    public GiftEventService(AppUserRepository users, GiftEventRepository events, ParticipantRepository participants,
                            GiftAssignmentRepository assignments) {
        this.users = users;
        this.events = events;
        this.participants = participants;
        this.assignments = assignments;
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

    @Transactional
    public DrawResponse draw(UUID organizerId, UUID eventId) {
        // The same row lock serializes closing, joining and competing draw requests.
        GiftEvent event = events.lockOwnedEvent(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono wydarzenia."));
        if (event.getStatus() != GiftEvent.Status.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Losowanie wymaga zamkniętej listy uczestników.");
        }
        List<Participant> eventParticipants = participants.findByEventIdOrderByCreatedAtAscIdAsc(eventId);
        if (eventParticipants.size() < 3) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wydarzenie wymaga co najmniej 3 uczestników.");
        }
        Map<UUID, Participant> byId = eventParticipants.stream()
                .collect(Collectors.toMap(Participant::getId, Function.identity()));
        List<GiftAssignment> results = drawAlgorithm.draw(eventParticipants.stream().map(Participant::getId).toList())
                .stream().map(pair -> new GiftAssignment(event, byId.get(pair.giverId()),
                        byId.get(pair.recipientId()))).toList();
        assignments.saveAllAndFlush(results);
        event.markDrawn();
        return new DrawResponse(event.getId(), event.getStatus());
    }
}
