package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {
    @Mock private GiftEventRepository events;
    @Mock private ParticipantRepository participants;
    @Mock private ParticipantAccessTokenRepository accessTokens;
    @InjectMocks private InvitationService service;

    @Test
    void returnsSeparateInvitationDtoAndStoresOnlyTokenHash() {
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockOwnedEvent(eventId, organizerId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.OPEN);

        var response = service.rotate(eventId, organizerId);

        assertThat(response.eventId()).isEqualTo(eventId);
        assertThat(response.token()).matches("[A-Za-z0-9_-]{43}");
        verify(event).replaceInvitationTokenHash(matches("[a-f0-9]{64}"));
    }

    @Test
    void publicDetailsKeepTheExistingResponseShape() {
        GiftEvent event = mock(GiftEvent.class);
        when(events.findByInvitationTokenHash(matches("[a-f0-9]{64}"))).thenReturn(Optional.of(event));
        when(event.getName()).thenReturn("Wigilia");
        when(event.getBudget()).thenReturn(new BigDecimal("150.00"));
        when(event.getStatus()).thenReturn(GiftEvent.Status.CLOSED);

        var response = service.details("A".repeat(43));

        assertThat(response.name()).isEqualTo("Wigilia");
        assertThat(response.budget()).isEqualByComparingTo("150.00");
        assertThat(response.canJoin()).isFalse();
    }

    @Test
    void everyJoinIssuesADifferent256BitTokenAndSavesOnlyItsHash() throws Exception {
        String invitation = AccessToken.generate();
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockInvitedEvent(AccessToken.hash(invitation))).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.OPEN);
        when(event.getName()).thenReturn("Wigilia");
        when(event.getBudget()).thenReturn(new BigDecimal("150.00"));
        when(participants.saveAndFlush(any(Participant.class))).thenAnswer(invocation -> {
            Participant participant = invocation.getArgument(0);
            ReflectionTestUtils.setField(participant, "id", UUID.randomUUID());
            return participant;
        });

        var first = service.join(invitation, "Kasia");
        var second = service.join(invitation, "Ola");
        var saved = ArgumentCaptor.forClass(ParticipantAccessToken.class);
        verify(accessTokens, times(2)).save(saved.capture());
        assertThat(first.participantAccessToken()).isNotEqualTo(second.participantAccessToken());
        assertThat(first.nickname()).isEqualTo("Kasia");
        assertThat(first.name()).isEqualTo("Wigilia");
        assertThat(first.budget()).isEqualByComparingTo("150.00");
        var responses = java.util.List.of(first, second);
        for (int i = 0; i < responses.size(); i++) {
            var response = responses.get(i);
            String token = response.participantAccessToken();
            assertThat(token).matches("[A-Za-z0-9_-]{43}").isNotEqualTo(invitation);
            assertThat(Base64.getUrlDecoder().decode(token)).hasSize(32);
            assertThat(response.toString()).doesNotContain(token);
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
            var access = saved.getAllValues().get(i);
            assertThat(ReflectionTestUtils.getField(access, "tokenHash")).isEqualTo(hash).isNotEqualTo(token);
            var participant = (Participant) ReflectionTestUtils.getField(access, "participant");
            assertThat(participant.getId()).isEqualTo(response.participantId());
        }
    }
}
