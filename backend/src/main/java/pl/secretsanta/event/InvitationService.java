package pl.secretsanta.event;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import pl.secretsanta.event.dto.InvitationDetails;
import pl.secretsanta.event.dto.InvitationLink;
import pl.secretsanta.event.dto.JoinResponse;

@Service
class InvitationService {
    private final GiftEventRepository events;
    private final ParticipantRepository participants;
    private final ParticipantAccessTokenRepository accessTokens;

    InvitationService(GiftEventRepository events, ParticipantRepository participants,
                      ParticipantAccessTokenRepository accessTokens) {
        this.events = events;
        this.participants = participants;
        this.accessTokens = accessTokens;
    }

    @Transactional
    public InvitationLink rotate(UUID eventId, UUID organizerId) {
        GiftEvent event = events.lockOwnedEvent(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono wydarzenia."));
        requireOpen(event);

        String token = AccessToken.generate();
        event.replaceInvitationTokenHash(AccessToken.hash(token));
        return new InvitationLink(eventId, token);
    }

    @Transactional(readOnly = true)
    public InvitationDetails details(String token) {
        GiftEvent event = events.findByInvitationTokenHash(invitationHash(token))
                .orElseThrow(InvitationService::invitationNotFound);
        return new InvitationDetails(event.getName(), event.getBudget(), isOpen(event));
    }

    @Transactional
    public JoinResponse join(String token, String nickname) {
        GiftEvent event = events.lockInvitedEvent(invitationHash(token))
                .orElseThrow(InvitationService::invitationNotFound);
        requireOpen(event);

        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Participant participant = new Participant(event, null, nickname, createdAt);
        try {
            participants.saveAndFlush(participant);
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "23505".equals(violation.getSQLState())
                        && "uk_participants_event_nickname_ci".equals(violation.getConstraintName())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ten pseudonim jest już zajęty.");
                }
            }
            throw exception;
        }
        String participantToken = AccessToken.generate();
        accessTokens.save(new ParticipantAccessToken(participant, AccessToken.hash(participantToken), createdAt));
        return new JoinResponse(participant.getId(), event.getId(), event.getName(), event.getBudget(),
                participant.getNickname(), participantToken);
    }

    private static boolean isOpen(GiftEvent event) {
        return event.getStatus() == GiftEvent.Status.OPEN;
    }

    private static void requireOpen(GiftEvent event) {
        if (!isOpen(event)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Wydarzenie jest zamknięte.");
        }
    }

    private static String invitationHash(String token) {
        if (!AccessToken.hasValidFormat(token)) {
            throw invitationNotFound();
        }
        return AccessToken.hash(token);
    }

    private static ResponseStatusException invitationNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zaproszenia.");
    }
}
