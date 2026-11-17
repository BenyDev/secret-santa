package pl.secretsanta.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.IOException;
import java.security.KeyPair;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
class JwtKeyConfiguration {
    @Bean
    KeyPair jwtKeyPair(
            @Value("${secretsanta.jwt.private-key}") Resource privateKeyLocation,
            @Value("${secretsanta.jwt.public-key}") Resource publicKeyLocation) throws IOException {
        try (var privatePem = privateKeyLocation.getInputStream();
             var publicPem = publicKeyLocation.getInputStream()) {
            var privateKey = RsaKeyConverters.pkcs8().convert(privatePem);
            var publicKey = RsaKeyConverters.x509().convert(publicPem);
            if (publicKey.getModulus().bitLength() < 2048 || !publicKey.getModulus().equals(privateKey.getModulus())) {
                throw new IllegalStateException("Configure a matching RSA key pair of at least 2048 bits.");
            }
            return new KeyPair(publicKey, privateKey);
        }
    }
}
