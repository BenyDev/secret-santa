package pl.secretsanta.event;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ParticipantAccessService {
    private final ParticipantAccessTokenRepository tokens;

    ParticipantAccessService(ParticipantAccessTokenRepository tokens) {
        this.tokens = tokens;
    }

    public UUID authenticate(String token) {
        if (!AccessToken.hasValidFormat(token)) {
            throw invalidToken();
        }
        return tokens.findParticipantIdByHash(AccessToken.hash(token))
                .orElseThrow(ParticipantAccessService::invalidToken);
    }

    private static BadCredentialsException invalidToken() {
        return new BadCredentialsException("Nieprawidłowe poświadczenie uczestnika.");
    }
}
