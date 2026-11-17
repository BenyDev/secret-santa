package pl.secretsanta.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface ParticipantAccessTokenRepository extends JpaRepository<ParticipantAccessToken, UUID> {
    @Query("select access.participant.id from ParticipantAccessToken access where access.tokenHash = :hash")
    Optional<UUID> findParticipantIdByHash(String hash);
}
