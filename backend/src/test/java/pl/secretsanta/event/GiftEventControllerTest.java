package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import org.springframework.test.web.servlet.MockMvc;

import pl.secretsanta.security.JwtConfiguration;
import pl.secretsanta.security.SecurityConfiguration;
import pl.secretsanta.security.TestJwtKeys;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GiftEventController.class)
@ActiveProfiles("test")
@Import({SecurityConfiguration.class, JwtConfiguration.class, TestJwtKeys.class})
class GiftEventControllerTest {
    private static final UUID ORGANIZER_ID = UUID.randomUUID();
    private static final String VALID_BODY = """
            {"name":"Wigilia 2026","budget":150.00,"organizerNickname":"Ben"}
            """;

    @Autowired private MockMvc mvc;
    @Autowired private JwtEncoder encoder;
    @MockitoBean private GiftEventService events;

    @Test
    void createsEventUsingTheJwtSubjectEvenIfTheBodySuppliesAnotherOrganizer() throws Exception {
        var request = new CreateGiftEventRequest("Wigilia 2026", new BigDecimal("150.00"), "Ben");
        UUID eventId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        Instant now = Instant.now();
        when(events.create(ORGANIZER_ID, request)).thenReturn(new GiftEventResponse(eventId, request.name(),
                request.budget(), GiftEvent.Status.OPEN, now, ORGANIZER_ID,
                new GiftEventResponse.OrganizerParticipant(participantId, ORGANIZER_ID, "Ben", now)));

        mvc.perform(post("/api/events").header(HttpHeaders.AUTHORIZATION, bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" Wigilia 2026 ","budget":150.00,"organizerNickname":" Ben ","organizerId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.name").value("Wigilia 2026"))
                .andExpect(jsonPath("$.budget").value(150.0))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").value(now.toString()))
                .andExpect(jsonPath("$.organizerId").value(ORGANIZER_ID.toString()))
                .andExpect(jsonPath("$.organizerParticipant.id").value(participantId.toString()))
                .andExpect(jsonPath("$.organizerParticipant.userId").value(ORGANIZER_ID.toString()))
                .andExpect(jsonPath("$.organizerParticipant.nickname").value("Ben"));
    }

    @Test
    void requiresJwt() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        verifyNoInteractions(events);
    }

    @Test
    void requiresJwtToReadEvents() throws Exception {
        mvc.perform(get("/api/events")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/events/{eventId}", UUID.randomUUID())).andExpect(status().isUnauthorized());
        verifyNoInteractions(events);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-10", "10.001", "100000000", "null"})
    void rejectsInvalidBudgets(String budget) throws Exception {
        String body = """
                {"name":"Wigilia 2026","budget":%s,"organizerNickname":"Ben"}
                """.formatted(budget);
        mvc.perform(post("/api/events").header(HttpHeaders.AUTHORIZATION, bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields[*].field", hasItem("budget")));
        verifyNoInteractions(events);
    }

    @Test
    void rejectsBlankNameAndNickname() throws Exception {
        mvc.perform(post("/api/events").header(HttpHeaders.AUTHORIZATION, bearer()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","budget":150.00,"organizerNickname":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[*].field", hasItem("name")))
                .andExpect(jsonPath("$.fields[*].field", hasItem("organizerNickname")));
        verifyNoInteractions(events);
    }

    private String bearer() {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("secret-santa").subject(ORGANIZER_ID.toString())
                .issuedAt(now).expiresAt(now.plusSeconds(900)).build();
        return "Bearer " + encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }
}
