package pl.secretsanta.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class InvitationController {
    private final InvitationService invitations;

    InvitationController(InvitationService invitations) {
        this.invitations = invitations;
    }

    @PostMapping("/api/events/{eventId}/invitation")
    public InvitationLink rotate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID eventId) {
        return invitations.rotate(eventId, UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/api/invitations/{token}")
    public InvitationDetails details(@PathVariable String token) {
        return invitations.details(token);
    }

    @PostMapping("/api/invitations/{token}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinResponse join(@PathVariable String token, @Valid @RequestBody JoinRequest request) {
        return invitations.join(token, request.nickname());
    }

    public record InvitationLink(UUID eventId, String token) {
        @Override
        public String toString() {
            return "InvitationLink[eventId=" + eventId + ", token=redacted]";
        }
    }

    public record InvitationDetails(String name, BigDecimal budget, boolean canJoin) {
    }

    public record JoinResponse(UUID participantId, UUID eventId, String name, BigDecimal budget) {
    }

    public record JoinRequest(
            @NotBlank(message = "Podaj pseudonim.")
            @Size(max = 60, message = "Pseudonim może mieć maksymalnie 60 znaków.")
            String nickname
    ) {
        public JoinRequest {
            if (nickname != null) {
                nickname = nickname.strip();
            }
        }
    }
}
