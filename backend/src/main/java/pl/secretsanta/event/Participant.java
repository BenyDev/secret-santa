package pl.secretsanta.event;

import jakarta.persistence.*;
import pl.secretsanta.user.AppUser;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private GiftEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false, length = 60)
    private String nickname;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(length = 2000)
    private String wishlist;

    protected Participant() {
    }

    Participant(GiftEvent event, AppUser user, String nickname, Instant createdAt) {
        this.event = event;
        this.user = user;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getNickname() { return nickname; }
    public Instant getCreatedAt() { return createdAt; }

    GiftEvent getEvent() { return event; }
    String getWishlist() { return wishlist; }

    void updateWishlist(String wishlist) {
        this.wishlist = wishlist;
    }
}
