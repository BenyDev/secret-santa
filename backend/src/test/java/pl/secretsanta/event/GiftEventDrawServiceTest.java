package pl.secretsanta.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.secretsanta.user.AppUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftEventDrawServiceTest {
    @Mock private AppUserRepository users;
    @Mock private GiftEventRepository events;
    @Mock private ParticipantRepository participants;
    @Mock private GiftAssignmentRepository assignments;
    @InjectMocks private GiftEventService service;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();

    @Test
    void rejectsForeignEventWithoutReadingParticipants() {
        when(events.lockOwnedEvent(eventId, ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.draw(ownerId, eventId))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("404 NOT_FOUND");
        verifyNoInteractions(participants, assignments);
    }

    @Test
    void rejectsOpenAndAlreadyDrawnEventsWithoutChangingAssignments() {
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockOwnedEvent(eventId, ownerId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.OPEN, GiftEvent.Status.DRAWN);

        assertThatThrownBy(() -> service.draw(ownerId, eventId))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409 CONFLICT");
        assertThatThrownBy(() -> service.draw(ownerId, eventId))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409 CONFLICT");
        verifyNoInteractions(participants, assignments);
    }

    @Test
    void savesEveryAssignmentBeforeChangingStatusAndNeverReturnsRecipients() {
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockOwnedEvent(eventId, ownerId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.CLOSED, GiftEvent.Status.DRAWN);
        when(event.getId()).thenReturn(eventId);
        List<Participant> people = List.of(participant(), participant(), participant(), participant());
        when(participants.findByEventIdOrderByCreatedAtAscIdAsc(eventId)).thenReturn(people);

        var response = service.draw(ownerId, eventId);

        assertThat(response.id()).isEqualTo(eventId);
        assertThat(response.status()).isEqualTo(GiftEvent.Status.DRAWN);
        assertThat(java.util.Arrays.stream(response.getClass().getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName).toList())
                .containsExactly("id", "status");
        var order = org.mockito.Mockito.inOrder(assignments, event);
        order.verify(assignments).saveAllAndFlush(org.mockito.ArgumentMatchers.argThat(
                saved -> ((List<?>) saved).size() == 4));
        order.verify(event).markDrawn();
    }

    @Test
    void rejectsTooFewParticipants() {
        GiftEvent event = mock(GiftEvent.class);
        when(events.lockOwnedEvent(eventId, ownerId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(GiftEvent.Status.CLOSED);
        when(participants.findByEventIdOrderByCreatedAtAscIdAsc(eventId))
                .thenReturn(List.of(mock(Participant.class), mock(Participant.class)));

        assertThatThrownBy(() -> service.draw(ownerId, eventId))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("409 CONFLICT");
        verifyNoInteractions(assignments);
        verify(event, never()).markDrawn();
    }

    private Participant participant() {
        Participant participant = mock(Participant.class);
        when(participant.getId()).thenReturn(UUID.randomUUID());
        return participant;
    }
}
