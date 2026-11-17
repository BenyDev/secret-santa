package pl.secretsanta.event;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.secretsanta.event.dto.ParticipantDrawResponse;
import pl.secretsanta.event.dto.ParticipantMeResponse;
import pl.secretsanta.event.dto.UpdateWishlistRequest;
import pl.secretsanta.event.dto.WishlistResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/participant")
public class ParticipantPanelController {
    private final ParticipantPanelService panel;

    ParticipantPanelController(ParticipantPanelService panel) {
        this.panel = panel;
    }

    @GetMapping("/me")
    public ParticipantMeResponse me(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        return panel.me(eventId, principal);
    }

    @GetMapping("/draw")
    public ParticipantDrawResponse draw(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        return panel.draw(eventId, principal);
    }

    @GetMapping("/wishlist")
    public WishlistResponse wishlist(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        return panel.wishlist(eventId, principal);
    }

    @PutMapping("/wishlist")
    public WishlistResponse updateWishlist(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal,
                                           @Valid @RequestBody UpdateWishlistRequest request) {
        return panel.updateWishlist(eventId, principal, request.wishlist());
    }
}
