package pl.secretsanta.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void returnsCreatedUserWithoutCredentials() throws Exception {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-11-17T12:00:00Z");
        when(authService.register(new RegisterRequest("test@example.com", "test-password")))
                .thenReturn(new RegisterResponse(id, "test@example.com", createdAt));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":" Test@EXAMPLE.com ","password":"test-password"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.passwordHash").doesNotHaveJsonPath());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', quoteCharacter = '\'', textBlock = """
            '{"email":"not-an-email","password":"test-password"}' | email
            '{"password":"test-password"}' | email
            '{"email":"test@example.com","password":"1234567"}' | password
            '{"email":"test@example.com"}' | password
            """)
    void reportsInvalidFields(String json, String field) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields[*].field", hasItem(field)));
        verifyNoInteractions(authService);
    }

    @Test
    void rejectsPasswordsOverBcryptByteLimit() throws Exception {
        String body = """
                {"email":"test@example.com","password":"%s"}
                """.formatted("ą".repeat(37));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[0].field").value("password"));
        verifyNoInteractions(authService);
    }

    @Test
    void returnsConflictForRegisteredEmail() throws Exception {
        when(authService.register(any())).thenThrow(new EmailAlreadyRegisteredException());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","password":"test-password"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void hidesInternalErrorDetails() throws Exception {
        when(authService.register(any())).thenThrow(new IllegalStateException("private technical details"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","password":"test-password"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Nie udało się zarejestrować konta."))
                .andExpect(jsonPath("$.trace").doesNotHaveJsonPath());
    }
}
