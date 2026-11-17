package pl.secretsanta.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    List<Participant> findByEventIdOrderByCreatedAtAscIdAsc(UUID eventId);

    long countByEventId(UUID eventId);
}
