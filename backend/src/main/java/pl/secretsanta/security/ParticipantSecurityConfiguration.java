package pl.secretsanta.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import pl.secretsanta.event.ParticipantAccessService;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class ParticipantSecurityConfiguration {
    private static final String PANEL = "/api/events/{eventId}/participant";

    @Bean
    @Order(1)
    SecurityFilterChain participantSecurityFilterChain(HttpSecurity http, ObjectMapper json, JwtDecoder decoder,
                                                       ParticipantAccessService access) throws Exception {
        var jwtProvider = new JwtAuthenticationProvider(decoder);
        AuthenticationManager bearerAuthentication = authentication -> {
            String token = ((BearerTokenAuthenticationToken) authentication).getToken();
            if (token.indexOf('.') >= 0) {
                return jwtProvider.authenticate(authentication);
            }
            return UsernamePasswordAuthenticationToken.authenticated(access.authenticate(token), null, List.of());
        };

        var unauthorized = SecurityConfiguration.unauthorized(json);
        var forbidden = SecurityConfiguration.forbidden(json);
        return SecurityConfiguration.statelessBearer(http.securityMatcher(PANEL + "/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, PANEL + "/me", PANEL + "/draw", PANEL + "/wishlist")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, PANEL + "/wishlist").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .oauth2ResourceServer(server -> server.authenticationManagerResolver(request -> bearerAuthentication)
                        .authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .build();
    }
}
