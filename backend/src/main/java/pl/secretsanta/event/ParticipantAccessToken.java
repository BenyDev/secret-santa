package pl.secretsanta.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participant_access_tokens")
class ParticipantAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false, unique = true)
    private Participant participant;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ParticipantAccessToken() {
    }

    ParticipantAccessToken(Participant participant, String tokenHash, Instant createdAt) {
        this.participant = participant;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
    }
}
