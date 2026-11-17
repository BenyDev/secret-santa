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

import java.util.UUID;

import pl.secretsanta.event.dto.InvitationDetails;
import pl.secretsanta.event.dto.InvitationLink;
import pl.secretsanta.event.dto.JoinRequest;
import pl.secretsanta.event.dto.JoinResponse;

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

}
