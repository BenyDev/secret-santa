package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import pl.secretsanta.security.JwtConfiguration;
import pl.secretsanta.security.ParticipantSecurityConfiguration;
import pl.secretsanta.security.SecurityConfiguration;
import pl.secretsanta.security.TestJwtKeys;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ParticipantPanelController.class, GiftEventController.class})
@ActiveProfiles("test")
@Import({SecurityConfiguration.class, ParticipantSecurityConfiguration.class, JwtConfiguration.class,
        TestJwtKeys.class, ParticipantAccessService.class, ParticipantPanelService.class})
class ParticipantPanelSecurityTest {
    private static final String PANEL = "/api/events/{eventId}/participant";

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private ParticipantAccessTokenRepository tokens;
    @MockitoBean private ParticipantRepository participants;
    @MockitoBean private GiftAssignmentRepository assignments;
    @MockitoBean private GiftEventService events;

    @Test
    void guestAccessIsBoundToTheirParticipantAndEventAndDoesNotReissueAToken() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID otherEvent = UUID.randomUUID();
        Participant guest = participant("Kasia", GiftEvent.Status.OPEN);
        String token = authorizeGuest(eventId, guest);

        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(get(PANEL + "/me", eventId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .queryParam("participantId", UUID.randomUUID().toString()))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, org.hamcrest.Matchers.containsString("no-store")))
                    .andExpect(content().json("""
                            {"participantId":"%s","eventId":"%s","nickname":"Kasia",
                            "name":"Wigilia","budget":150,"status":"OPEN"}
                            """.formatted(guest.getId(), eventId), JsonCompareMode.STRICT));
        }
        mvc.perform(get(PANEL + "/me", otherEvent).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
        mvc.perform(get(PANEL + "/me", eventId).queryParam("token", token))
                .andExpect(status().isUnauthorized());
        verify(tokens, org.mockito.Mockito.times(3)).findParticipantIdByHash(AccessToken.hash(token));
        verifyNoMoreInteractions(tokens);
    }

    @Test
    void invitationAndParticipantCredentialsCannotCrossSecurityBoundaries() throws Exception {
        UUID eventId = UUID.randomUUID();
        String invitation = AccessToken.generate();
        String token = AccessToken.generate();
        when(tokens.findParticipantIdByHash(AccessToken.hash(token))).thenReturn(Optional.of(UUID.randomUUID()));

        mvc.perform(get(PANEL + "/me", eventId).header(HttpHeaders.AUTHORIZATION, "Bearer " + invitation))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(PANEL + "/me", eventId).header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/events/{eventId}/draw", eventId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
        verify(tokens).findParticipantIdByHash(AccessToken.hash(invitation));
        verifyNoMoreInteractions(tokens);
        verifyNoInteractions(events, participants);
    }

    @Test
    void organizerJwtOnlyResolvesTheOrganizersOwnParticipantInThatEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        Participant organizer = participant("Ben", GiftEvent.Status.DRAWN);
        Participant guest = participant("Kasia", GiftEvent.Status.DRAWN);
        when(participants.findOrganizerParticipant(eventId, organizerId)).thenReturn(Optional.of(organizer));
        when(assignments.findRecipient(eventId, organizer.getId())).thenReturn(Optional.of(guest));
        String bearer = organizerBearer(organizerId);

        mvc.perform(get(PANEL + "/me", eventId).header(HttpHeaders.AUTHORIZATION, bearer)
                        .queryParam("participantId", guest.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId").value(organizer.getId().toString()));
        mvc.perform(get(PANEL + "/draw", eventId).header(HttpHeaders.AUTHORIZATION, bearer)
                        .queryParam("giverId", guest.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientId").value(guest.getId().toString()));
        mvc.perform(get(PANEL + "/me", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNotFound());
        verify(assignments).findRecipient(eventId, organizer.getId());
        verifyNoMoreInteractions(assignments);
        verifyNoInteractions(tokens);
    }

    @Test
    void hidesDrawUntilDrawnAndThenReturnsOnlyTheAuthenticatedGiversRecipientAndWishlist() throws Exception {
        UUID eventId = UUID.randomUUID();
        Participant guest = participant("Kasia", GiftEvent.Status.CLOSED);
        Participant recipient = participant("Ola", GiftEvent.Status.DRAWN);
        recipient.updateWishlist("Książka");
        String bearer = "Bearer " + authorizeGuest(eventId, guest);

        mvc.perform(get(PANEL + "/draw", eventId).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isConflict());
        verifyNoInteractions(assignments);

        guest.getEvent().markDrawn();
        when(assignments.findRecipient(eventId, guest.getId())).thenReturn(Optional.of(recipient));
        mvc.perform(get(PANEL + "/draw", eventId).header(HttpHeaders.AUTHORIZATION, bearer)
                        .queryParam("giverId", recipient.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"recipientId":"%s","recipientNickname":"Ola","wishlist":"Książka"}
                        """.formatted(recipient.getId()), JsonCompareMode.STRICT));
        verify(assignments).findRecipient(eventId, guest.getId());
        verifyNoMoreInteractions(assignments);
    }

    @Test
    void wishlistReadAndWriteUseTheAuthenticatedParticipant() throws Exception {
        UUID eventId = UUID.randomUUID();
        Participant guest = participant("Kasia", GiftEvent.Status.OPEN);
        String bearer = "Bearer " + authorizeGuest(eventId, guest);
        mvc.perform(put(PANEL + "/wishlist", eventId).header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"wishlist":" Książka ","participantId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk());
        mvc.perform(get(PANEL + "/wishlist", eventId).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"wishlist":"Książka"}
                        """, JsonCompareMode.STRICT));
        verifyNoInteractions(assignments);
    }

    private String authorizeGuest(UUID eventId, Participant guest) {
        String token = AccessToken.generate();
        when(tokens.findParticipantIdByHash(AccessToken.hash(token))).thenReturn(Optional.of(guest.getId()));
        when(participants.findByIdAndEventId(guest.getId(), eventId)).thenReturn(Optional.of(guest));
        return token;
    }

    private static Participant participant(String nickname, GiftEvent.Status eventStatus) {
        GiftEvent event = new GiftEvent("Wigilia", new BigDecimal("150.00"), null, Instant.now());
        if (eventStatus == GiftEvent.Status.CLOSED) {
            event.close();
        } else if (eventStatus == GiftEvent.Status.DRAWN) {
            event.markDrawn();
        }
        Participant participant = new Participant(event, null, nickname, Instant.now());
        ReflectionTestUtils.setField(participant, "id", UUID.randomUUID());
        return participant;
    }

    private String organizerBearer(UUID organizerId) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("secret-santa").subject(organizerId.toString())
                .issuedAt(now).expiresAt(now.plusSeconds(900)).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }
}
