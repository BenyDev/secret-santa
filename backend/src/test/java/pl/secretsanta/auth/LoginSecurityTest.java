package pl.secretsanta.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.secretsanta.health.HealthController;
import pl.secretsanta.security.JwtConfiguration;
import pl.secretsanta.security.SecurityConfiguration;
import pl.secretsanta.security.TestJwtKeys;
import pl.secretsanta.user.AppUser;
import pl.secretsanta.user.AppUserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, HealthController.class})
@ActiveProfiles("test")
@Import({SecurityConfiguration.class, JwtConfiguration.class, TestJwtKeys.class, LoginService.class,
        PasswordConfiguration.class, AccessTokenService.class})
class LoginSecurityTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "test-password";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JwtEncoder encoder;
    @Autowired private JwtDecoder decoder;
    @Autowired private PasswordEncoder passwordEncoder;
    @MockitoBean private AppUserRepository users;
    @MockitoBean private AuthService registration;

    @BeforeEach
    void configureUser() {
        AppUser user = mock(AppUser.class);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getEmail()).thenReturn(EMAIL);
        when(user.getPasswordHash()).thenReturn(passwordEncoder.encode(PASSWORD));
        when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @Test
    void logsInAndUsesVerifiedSubjectForMe() throws Exception {
        String response = login(" Test@EXAMPLE.COM ", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.user.email").value(EMAIL))
                .andExpect(jsonPath("$..password").isEmpty())
                .andExpect(jsonPath("$..passwordHash").isEmpty())
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(response).get("accessToken").asText();
        var jwt = decoder.decode(token);
        assertThat(jwt.getHeaders().get("alg")).isEqualTo("RS256");
        assertThat(jwt.getSubject()).isEqualTo(USER_ID.toString());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("secret-santa");
        assertThat(jwt.getExpiresAt()).isEqualTo(jwt.getIssuedAt().plusSeconds(900));

        mvc.perform(get("/api/auth/me").param("id", UUID.randomUUID().toString()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.passwordHash").doesNotHaveJsonPath());
    }

    @Test
    void returnsTheSameErrorForWrongPasswordAndUnknownEmail() throws Exception {
        String expected = """
                {"code":"INVALID_CREDENTIALS","message":"Nieprawidłowy e-mail lub hasło"}
                """;
        login(EMAIL, "wrong-password").andExpect(status().isUnauthorized()).andExpect(content().json(expected));
        login("unknown@example.com", PASSWORD).andExpect(status().isUnauthorized()).andExpect(content().json(expected));
    }

    @Test
    void rejectsMissingTamperedAndExpiredTokens() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        String valid = signedToken(Instant.now().plusSeconds(60));
        int signatureStart = valid.lastIndexOf('.') + 1;
        char replacement = valid.charAt(signatureStart) == 'A' ? 'B' : 'A';
        String tampered = valid.substring(0, signatureStart) + replacement + valid.substring(signatureStart + 1);
        for (String token : new String[]{tampered, signedToken(Instant.now().minusSeconds(2)), "invalid"}) {
            mvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        }
    }

    @Test
    void keepsPublicRoutesOpenAndProtectsOtherApiRoutes() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        when(registration.register(new RegisterRequest(EMAIL, PASSWORD)))
                .thenReturn(new RegisterResponse(USER_ID, EMAIL, Instant.now()));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new RegisterRequest(EMAIL, PASSWORD))))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/private")).andExpect(status().isUnauthorized());
        mvc.perform(get("/forbidden").header(HttpHeaders.AUTHORIZATION, "Bearer " + signedToken(Instant.now().plusSeconds(60))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(new LoginRequest(email, password))));
    }

    private String signedToken(Instant expiration) {
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(),
                JwtClaimsSet.builder().issuer("secret-santa").subject(USER_ID.toString())
                        .issuedAt(expiration.minusSeconds(900)).expiresAt(expiration).build())).getTokenValue();
    }
}
