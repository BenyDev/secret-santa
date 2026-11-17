package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantAccessServiceTest {
    @Mock private ParticipantAccessTokenRepository tokens;
    @InjectMocks private ParticipantAccessService service;

    @Test
    void validTokenResolvesToTheParticipantBoundToItsHash() {
        String token = AccessToken.generate();
        UUID participantId = UUID.randomUUID();
        when(tokens.findParticipantIdByHash(AccessToken.hash(token))).thenReturn(Optional.of(participantId));

        assertThat(service.authenticate(token)).isEqualTo(participantId);
    }

    @Test
    void rejectsAnUnknownTokenWithValidFormat() {
        assertThatThrownBy(() -> service.authenticate(AccessToken.generate()))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsInvalidFormatBeforeLookingUpTheHash() {
        assertThatThrownBy(() -> service.authenticate("invalid"))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(tokens);
    }
}
