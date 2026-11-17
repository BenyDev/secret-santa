package pl.secretsanta.event;

import pl.secretsanta.event.dto.CreateGiftEventRequest;
import pl.secretsanta.event.dto.GiftEventResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.secretsanta.security.TestJwtKeys;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestJwtKeys.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class InvitationPostgresTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private AppUserRepository users;
    @Autowired private GiftEventService events;
    @Autowired private JwtEncoder encoder;

    @Test
    void onlyTheOrganizerCanIssueAnInvitationAndOnlyItsHashIsStored() throws Exception {
        var event = createEvent();
        mvc.perform(post("/api/events/{id}/invitation", event.id())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/events/{id}/invitation", event.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(createUser())))
                .andExpect(status().isNotFound());

        String token = issue(event);
        assertThat(token).matches("[A-Za-z0-9_-]{43}");
        String expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.US_ASCII)));
        assertThat(jdbc.queryForObject("SELECT invitation_token_hash FROM secret_santa.gift_events WHERE id = ?",
                String.class, event.id())).isEqualTo(expectedHash);
    }

    @Test
    void guestCanReadPublicDetailsAndJoinWithoutAnAccount() throws Exception {
        var event = createEvent();
        String token = issue(event);
        mvc.perform(get("/api/invitations/{token}", token)).andExpect(status().isOk())
                .andExpect(content().json("""
                        {"name":"Wigilia","budget":150.00,"canJoin":true}
                        """, JsonCompareMode.STRICT));
        String response = join(token, "  Kasia  ").andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(event.id().toString()))
                .andReturn().getResponse().getContentAsString();
        UUID participantId = UUID.fromString(json.readTree(response).get("participantId").asText());
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM secret_santa.participants
                WHERE id = ? AND event_id = ? AND nickname = 'Kasia' AND user_id IS NULL
                """, Long.class, participantId, event.id())).isEqualTo(1L);

        join(token, "   ").andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields[0].field").value("nickname"));
        join(token, "a".repeat(61)).andExpect(status().isBadRequest());
        join(token, "kASIA").andExpect(status().isConflict());
        join(token, "ben").andExpect(status().isConflict());
        mvc.perform(get("/api/invitations/{token}", "invalid")).andExpect(status().isNotFound());
        join("A".repeat(43), "Ola").andExpect(status().isNotFound());
    }

    @Test
    void rotationInvalidatesTheOldTokenAndClosedEventRejectsJoin() throws Exception {
        var event = createEvent();
        String previous = issue(event);
        String current = issue(event);
        assertThat(current).isNotEqualTo(previous);
        mvc.perform(get("/api/invitations/{token}", previous)).andExpect(status().isNotFound());
        join(previous, "Ola").andExpect(status().isNotFound());
        mvc.perform(get("/api/invitations/{token}", current)).andExpect(jsonPath("$.canJoin").value(true));

        jdbc.update("UPDATE secret_santa.gift_events SET status = 'CLOSED' WHERE id = ?", event.id());
        mvc.perform(get("/api/invitations/{token}", current)).andExpect(status().isOk())
                .andExpect(jsonPath("$.canJoin").value(false));
        join(current, "Ola").andExpect(status().isConflict());
    }

    @Test
    void concurrentGuestsCannotUseTheSameNicknameAndDatabaseAlsoEnforcesIt() throws Exception {
        var event = createEvent();
        String token = issue(event);
        var start = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return join(token, "Łucja").andReturn().getResponse().getStatus();
            });
            var second = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return join(token, "łUCJA").andReturn().getResponse().getStatus();
            });
            assertThat(new int[] {first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(201, 409);
        }

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO secret_santa.participants (id, event_id, nickname, created_at)
                VALUES (?, ?, 'ŁUCJA', now())
                """, UUID.randomUUID(), event.id()))
                .isInstanceOf(DuplicateKeyException.class)
                .hasStackTraceContaining("uk_participants_event_nickname_ci");
    }

    private GiftEventResponse createEvent() {
        return events.create(createUser(), new CreateGiftEventRequest("Wigilia", new BigDecimal("150.00"), "Ben"));
    }

    private UUID createUser() {
        return users.save(new AppUser(UUID.randomUUID() + "@example.com", "fixture-hash", Instant.now())).getId();
    }

    private String issue(GiftEventResponse event) throws Exception {
        String response = mvc.perform(post("/api/events/{id}/invitation", event.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(event.organizerId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventId").value(event.id().toString()))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    private ResultActions join(String token, String nickname) throws Exception {
        return mvc.perform(post("/api/invitations/{token}/join", token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("nickname", nickname))));
    }

    private String bearerFor(UUID userId) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("secret-santa").subject(userId.toString())
                .issuedAt(now).expiresAt(now.plusSeconds(900)).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }
}
