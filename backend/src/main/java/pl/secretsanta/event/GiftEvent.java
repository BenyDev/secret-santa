package pl.secretsanta.event;

import jakarta.persistence.*;
import pl.secretsanta.user.AppUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gift_events")
public class GiftEvent {
    public enum Status { OPEN, CLOSED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private AppUser organizer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "invitation_token_hash", length = 64)
    private String invitationTokenHash;

    protected GiftEvent() {
    }

    GiftEvent(String name, BigDecimal budget, AppUser organizer, Instant createdAt) {
        this.name = name;
        this.budget = budget;
        this.organizer = organizer;
        this.status = Status.OPEN;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBudget() { return budget; }
    public AppUser getOrganizer() { return organizer; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    void replaceInvitationTokenHash(String hash) {
        this.invitationTokenHash = hash;
    }
}
