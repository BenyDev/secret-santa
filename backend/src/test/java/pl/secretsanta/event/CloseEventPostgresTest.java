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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.secretsanta.security.TestJwtKeys;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestJwtKeys.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class CloseEventPostgresTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private AppUserRepository users;
    @Autowired private GiftEventService events;
    @Autowired private JwtEncoder encoder;

    @Test
    void onlyOwnerCanCloseWithAtLeastThreeParticipantsAndCannotCloseAgain() throws Exception {
        var event = createEvent();
        mvc.perform(post("/api/events/{id}/close", event.id())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/events/{id}/close", event.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(createUser())))
                .andExpect(status().isNotFound());
        close(event).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Wydarzenie wymaga co najmniej 3 uczestników."));

        String token = issue(event);
        join(token, "Kasia").andExpect(status().isCreated());
        join(token, "Ola").andExpect(status().isCreated());
        close(event).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(event.id().toString()))
                .andExpect(jsonPath("$.status").value("CLOSED"));
        close(event).andExpect(status().isConflict());
        mvc.perform(get("/api/invitations/{token}", token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.canJoin").value(false));
        join(token, "Nowa osoba").andExpect(status().isConflict());
        assertThat(participantCount(event.id())).isEqualTo(3);
    }

    @Test
    void concurrentJoinAndCloseKeepParticipantListConsistent() throws Exception {
        var event = createEvent();
        String token = issue(event);
        join(token, "Kasia").andExpect(status().isCreated());
        var start = new CyclicBarrier(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var closing = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return close(event).andReturn().getResponse().getStatus();
            });
            var joining = executor.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return join(token, "Ola").andReturn().getResponse().getStatus();
            });
            int closeStatus = closing.get(20, TimeUnit.SECONDS);
            int joinStatus = joining.get(20, TimeUnit.SECONDS);
            assertThat(joinStatus).isEqualTo(201);
            assertThat(closeStatus).isIn(200, 409);
            assertThat(participantCount(event.id())).isEqualTo(3);
            assertThat(jdbc.queryForObject("SELECT status FROM secret_santa.gift_events WHERE id = ?",
                    String.class, event.id())).isEqualTo(closeStatus == 200 ? "CLOSED" : "OPEN");
        }
    }

    private GiftEventResponse createEvent() {
        return events.create(createUser(), new CreateGiftEventRequest("Wigilia", new BigDecimal("150.00"), "Ben"));
    }

    private UUID createUser() {
        return users.save(new AppUser(UUID.randomUUID() + "@example.com", "fixture-hash", Instant.now())).getId();
    }

    private org.springframework.test.web.servlet.ResultActions close(GiftEventResponse event) throws Exception {
        return mvc.perform(post("/api/events/{id}/close", event.id())
                .header(HttpHeaders.AUTHORIZATION, bearerFor(event.organizerId())));
    }

    private String issue(GiftEventResponse event) throws Exception {
        String response = mvc.perform(post("/api/events/{id}/invitation", event.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(event.organizerId())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions join(String token, String nickname) throws Exception {
        return mvc.perform(post("/api/invitations/{token}/join", token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("nickname", nickname))));
    }

    private long participantCount(UUID eventId) {
        return jdbc.queryForObject("SELECT count(*) FROM secret_santa.participants WHERE event_id = ?",
                Long.class, eventId);
    }

    private String bearerFor(UUID userId) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("secret-santa").subject(userId.toString())
                .issuedAt(now).expiresAt(now.plusSeconds(900)).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }
}
