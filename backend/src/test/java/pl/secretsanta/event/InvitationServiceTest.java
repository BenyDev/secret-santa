package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {
    @Mock private GiftEventRepository events;
    @Mock private ParticipantRepository participants;
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
}
