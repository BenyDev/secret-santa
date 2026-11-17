package pl.secretsanta.event;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.secretsanta.event.dto.ParticipantDrawResponse;
import pl.secretsanta.event.dto.ParticipantMeResponse;
import pl.secretsanta.event.dto.WishlistResponse;

import java.util.UUID;

@Service
class ParticipantPanelService {
    private final ParticipantRepository participants;
    private final GiftAssignmentRepository assignments;

    ParticipantPanelService(ParticipantRepository participants, GiftAssignmentRepository assignments) {
        this.participants = participants;
        this.assignments = assignments;
    }

    @Transactional(readOnly = true)
    public ParticipantMeResponse me(UUID eventId, Object principal) {
        Participant participant = findSelf(eventId, principal);
        GiftEvent event = participant.getEvent();
        return new ParticipantMeResponse(participant.getId(), eventId, participant.getNickname(), event.getName(),
                event.getBudget(), event.getStatus());
    }

    @Transactional(readOnly = true)
    public ParticipantDrawResponse draw(UUID eventId, Object principal) {
        Participant participant = findSelf(eventId, principal);
        if (participant.getEvent().getStatus() != GiftEvent.Status.DRAWN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Losowanie jeszcze się nie odbyło.");
        }
        Participant recipient = assignments.findRecipient(eventId, participant.getId())
                .orElseThrow(ParticipantPanelService::notFound);
        return new ParticipantDrawResponse(recipient.getId(), recipient.getNickname(), recipient.getWishlist());
    }

    @Transactional(readOnly = true)
    public WishlistResponse wishlist(UUID eventId, Object principal) {
        return new WishlistResponse(findSelf(eventId, principal).getWishlist());
    }

    @Transactional
    public WishlistResponse updateWishlist(UUID eventId, Object principal, String wishlist) {
        Participant participant = findSelf(eventId, principal);
        String content = wishlist.strip();
        participant.updateWishlist(content.isEmpty() ? null : content);
        return new WishlistResponse(participant.getWishlist());
    }

    private Participant findSelf(UUID eventId, Object principal) {
        return switch (principal) {
            case UUID participantId -> participants.findByIdAndEventId(participantId, eventId)
                    .orElseThrow(ParticipantPanelService::notFound);
            case Jwt jwt -> participants.findOrganizerParticipant(eventId, UUID.fromString(jwt.getSubject()))
                    .orElseThrow(ParticipantPanelService::notFound);
            case null, default -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        };
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono panelu uczestnika.");
    }
}
