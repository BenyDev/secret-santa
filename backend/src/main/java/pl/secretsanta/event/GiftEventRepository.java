package pl.secretsanta.event;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GiftEventRepository extends JpaRepository<GiftEvent, UUID> {
    @Query("""
            select new pl.secretsanta.event.GiftEventSummary(
                event.id, event.name, event.budget, event.status, count(participant.id), event.createdAt)
            from GiftEvent event
            left join Participant participant on participant.event = event
            where event.organizer.id = :organizerId
            group by event.id, event.name, event.budget, event.status, event.createdAt
            order by event.createdAt desc, event.id desc
            """)
    List<GiftEventSummary> findSummariesByOrganizerId(@Param("organizerId") UUID organizerId);

    Optional<GiftEvent> findByIdAndOrganizerId(UUID id, UUID organizerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from GiftEvent e where e.id = :eventId and e.organizer.id = :organizerId")
    Optional<GiftEvent> lockOwnedEvent(@Param("eventId") UUID eventId, @Param("organizerId") UUID organizerId);

    Optional<GiftEvent> findByInvitationTokenHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from GiftEvent e where e.invitationTokenHash = :hash")
    Optional<GiftEvent> lockInvitedEvent(@Param("hash") String hash);
}
