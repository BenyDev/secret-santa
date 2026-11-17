package pl.secretsanta.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface GiftAssignmentRepository extends JpaRepository<GiftAssignment, UUID> {
    @Query("""
            select assignment.recipient from GiftAssignment assignment
            where assignment.event.id = :eventId and assignment.giver.id = :giverId
            """)
    Optional<Participant> findRecipient(UUID eventId, UUID giverId);
}
