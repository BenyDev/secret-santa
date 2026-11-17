package pl.secretsanta.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    List<Participant> findByEventIdOrderByCreatedAtAscIdAsc(UUID eventId);

    long countByEventId(UUID eventId);

    Optional<Participant> findByIdAndEventId(UUID id, UUID eventId);

    @Query("""
            select participant from Participant participant
            where participant.event.id = :eventId and participant.user.id = :organizerId
                and participant.event.organizer.id = :organizerId
            """)
    Optional<Participant> findOrganizerParticipant(UUID eventId, UUID organizerId);
}
