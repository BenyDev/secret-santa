package pl.secretsanta.event;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import pl.secretsanta.security.TestJwtKeys;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestJwtKeys.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class GiftEventPostgresTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private TransactionTemplate transactions;
    @Autowired private AppUserRepository users;
    @Autowired private GiftEventService events;

    @Test
    void persistsTheOrganizerAndTheirParticipantAndAllowsGuestWithoutAccount() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String credentials = """
                {"email":"%s","password":"test-password"}
                """.formatted(email);
        String registration = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String userId = json.readTree(registration).get("id").asText();
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String bearer = "Bearer " + json.readTree(login).get("accessToken").asText();

        for (int i = 0; i < 2; i++) {
            String response = mvc.perform(post("/api/events").header(HttpHeaders.AUTHORIZATION, bearer)
                            .contentType(MediaType.APPLICATION_JSON).content("""
                                    {"name":"Wigilia 2026","budget":150.00,"organizerNickname":"Ben"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.organizerId").value(userId))
                    .andExpect(jsonPath("$.organizerParticipant.userId").value(userId))
                    .andReturn().getResponse().getContentAsString();
            var body = json.readTree(response);
            UUID eventId = UUID.fromString(body.get("id").asText());
            UUID participantId = UUID.fromString(body.get("organizerParticipant").get("id").asText());
            assertThat(jdbc.queryForObject("SELECT organizer_id FROM secret_santa.gift_events WHERE id = ?",
                    UUID.class, eventId)).isEqualTo(UUID.fromString(userId));
            assertThat(jdbc.queryForList("SELECT id FROM secret_santa.participants WHERE event_id = ?", UUID.class, eventId))
                    .containsExactly(participantId);
            assertThat(jdbc.queryForObject("SELECT user_id FROM secret_santa.participants WHERE id = ?",
                    UUID.class, participantId)).isEqualTo(UUID.fromString(userId));
            assertThat(jdbc.update("""
                    INSERT INTO secret_santa.participants (id, event_id, nickname, created_at)
                    VALUES (?, ?, 'Gość', now())
                    """, UUID.randomUUID(), eventId)).isEqualTo(1);
        }
    }

    @Test
    void rollsBackTheEventIfParticipantInsertFails() {
        UUID owner = createUser();
        String name = "Rollback " + UUID.randomUUID();
        // Bypass HTTP validation to force a real database error on the participant insert.
        assertThatThrownBy(() -> events.create(owner, new CreateGiftEventRequest(name, BigDecimal.TEN, "")))
                .hasStackTraceContaining("participants_nickname_check");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM secret_santa.gift_events WHERE name = ?", Long.class, name))
                .isZero();
    }

    @Test
    void databaseRejectsCommittingAnEventWithoutItsOrganizerParticipant() {
        UUID owner = createUser();
        UUID eventId = UUID.randomUUID();
        assertThatThrownBy(() -> transactions.executeWithoutResult(transaction -> jdbc.update("""
                INSERT INTO secret_santa.gift_events (id, name, budget, organizer_id, status, created_at)
                VALUES (?, 'Bez uczestnika', 150, ?, 'OPEN', now())
                """, eventId, owner))).hasStackTraceContaining("fk_event_organizer_participant");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM secret_santa.gift_events WHERE id = ?", Long.class, eventId))
                .isZero();
    }

    private UUID createUser() {
        return users.save(new AppUser(UUID.randomUUID() + "@example.com", "fixture-hash", Instant.now())).getId();
    }
}
