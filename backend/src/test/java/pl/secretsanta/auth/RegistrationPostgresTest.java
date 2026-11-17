package pl.secretsanta.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.secretsanta.security.TestJwtKeys;

import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestJwtKeys.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class RegistrationPostgresTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoSpyBean
    private PasswordEncoder passwordEncoder;

    @Test
    void persistsNormalizedEmailAndBcryptHash() throws Exception {
        String localPart = UUID.randomUUID().toString();
        String email = localPart + "@example.com";
        String password = "test-password";

        register("  " + localPart + "@EXAMPLE.COM  ", password)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.passwordHash").doesNotHaveJsonPath());

        String hash = jdbc.queryForObject(
                "SELECT password_hash FROM secret_santa.app_users WHERE email = ?", String.class, email);
        assertThat(hash).isNotEqualTo(password).hasSize(60);
        assertThat(new BCryptPasswordEncoder().matches(password, hash)).isTrue();
        register(email, password).andExpect(status().isConflict());
    }

    @Test
    void databaseRejectsConcurrentRegistrationAfterBothExistenceChecks() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String password = "race-test-password";
        CyclicBarrier bothPassedExistenceCheck = new CyclicBarrier(2);
        doAnswer(invocation -> {
            bothPassedExistenceCheck.await(10, TimeUnit.SECONDS);
            return invocation.callRealMethod();
        }).when(passwordEncoder).encode(password);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> register(email, password).andReturn().getResponse());
            var second = executor.submit(() -> register(email, password).andReturn().getResponse());
            var firstResponse = first.get(20, TimeUnit.SECONDS);
            var secondResponse = second.get(20, TimeUnit.SECONDS);

            assertThat(new int[] {firstResponse.getStatus(), secondResponse.getStatus()})
                    .containsExactlyInAnyOrder(201, 409);
            var conflict = firstResponse.getStatus() == 409 ? firstResponse : secondResponse;
            assertThat(conflict.getContentAsString()).contains("EMAIL_ALREADY_REGISTERED");
        }

        Long count = jdbc.queryForObject(
                "SELECT count(*) FROM secret_santa.app_users WHERE email = ?", Long.class, email);
        assertThat(count).isEqualTo(1L);
    }

    private ResultActions register(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }
}
