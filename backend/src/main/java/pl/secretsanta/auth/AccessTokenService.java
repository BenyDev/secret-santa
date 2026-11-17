package pl.secretsanta.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
class AccessTokenService {
    static final Duration TTL = Duration.ofMinutes(15);

    private final JwtEncoder encoder;
    private final String issuer;

    AccessTokenService(JwtEncoder encoder, @Value("${spring.application.name}") String issuer) {
        this.encoder = encoder;
        this.issuer = issuer;
    }

    String issue(UUID userId) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(TTL))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
                .getTokenValue();
    }
}
