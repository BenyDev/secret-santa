package pl.secretsanta.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper json) throws Exception {
        AuthenticationEntryPoint unauthorized = unauthorized(json);
        AccessDeniedHandler forbidden = forbidden(json);

        return statelessBearer(http)
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/invitations/{token}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/invitations/{token}/join").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .oauth2ResourceServer(server -> server.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .build();
    }

    static HttpSecurity statelessBearer(HttpSecurity http) throws Exception {
        return http
                // Credentials are sent explicitly in Authorization.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
    }

    static AuthenticationEntryPoint unauthorized(ObjectMapper json) {
        return (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            writeError(json, response, 401, "UNAUTHENTICATED", "Wymagane jest uwierzytelnienie.");
        };
    }

    static AccessDeniedHandler forbidden(ObjectMapper json) {
        return (request, response, exception) ->
                writeError(json, response, 403, "FORBIDDEN", "Brak uprawnień do tego zasobu.");
    }

    private static void writeError(ObjectMapper json, HttpServletResponse response, int status, String code,
                                   String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(response.getOutputStream(), Map.of("code", code, "message", message));
    }
}
