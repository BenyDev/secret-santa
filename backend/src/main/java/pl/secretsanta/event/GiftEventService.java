package pl.secretsanta.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
                new GiftEventResponse.OrganizerParticipant(participant.getId(), organizer.getId(),
                        participant.getNickname(), participant.getCreatedAt()));
    }
}
