package pl.secretsanta.event;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
class InvitationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern TOKEN_FORMAT = Pattern.compile("[A-Za-z0-9_-]{43}");

    private final GiftEventRepository events;
    private final ParticipantRepository participants;

    InvitationService(GiftEventRepository events, ParticipantRepository participants) {
        this.events = events;
        this.participants = participants;
    }

    @Transactional
    public InvitationController.InvitationLink rotate(UUID eventId, UUID organizerId) {
        GiftEvent event = events.lockOwnedEvent(eventId, organizerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono wydarzenia."));
        requireOpen(event);

        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        event.replaceInvitationTokenHash(hash(token));
        return new InvitationController.InvitationLink(eventId, token);
    }

    @Transactional(readOnly = true)
    public InvitationController.InvitationDetails details(String token) {
        GiftEvent event = events.findByInvitationTokenHash(invitationHash(token))
                .orElseThrow(InvitationService::invitationNotFound);
        return new InvitationController.InvitationDetails(event.getName(), event.getBudget(), isOpen(event));
    }

    @Transactional
    public InvitationController.JoinResponse join(String token, String nickname) {
        GiftEvent event = events.lockInvitedEvent(invitationHash(token))
                .orElseThrow(InvitationService::invitationNotFound);
        requireOpen(event);

        Participant participant = new Participant(event, null, nickname, Instant.now().truncatedTo(ChronoUnit.MICROS));
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
        return new InvitationController.JoinResponse(
                participant.getId(), event.getId(), event.getName(), event.getBudget());
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
        if (!TOKEN_FORMAT.matcher(token).matches()) {
            throw invitationNotFound();
        }
        return hash(token);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static ResponseStatusException invitationNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zaproszenia.");
    }
}
