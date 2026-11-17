package pl.secretsanta.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface GiftAssignmentRepository extends JpaRepository<GiftAssignment, UUID> {
}
