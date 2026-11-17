package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.secretsanta.user.AppUserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftEventServiceTest {
    @Mock private AppUserRepository users;
    @Mock private GiftEventRepository events;
    @Mock private ParticipantRepository participants;
    @InjectMocks private GiftEventService service;

    @Test
    void closesOwnedOpenEventWithThreeParticipants() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockOwnedEvent(eventId, organizerId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.OPEN, GiftEvent.Status.CLOSED);
        when(event.getId()).thenReturn(eventId);
        when(participants.countByEventId(eventId)).thenReturn(3L);

        var response = service.close(organizerId, eventId);

        assertThat(response.id()).isEqualTo(eventId);
        assertThat(response.status()).isEqualTo(GiftEvent.Status.CLOSED);
        verify(event).close();
    }

    @Test
    void rejectsClosingWhenOnlyTwoParticipantsExist() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockOwnedEvent(eventId, organizerId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.OPEN);
        when(participants.countByEventId(eventId)).thenReturn(2L);

        assertThatThrownBy(() -> service.close(organizerId, eventId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409 CONFLICT");
    }
}
