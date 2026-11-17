package pl.secretsanta.event;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "gift_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_gift_assignments_giver", columnNames = {"event_id", "giver_id"}),
        @UniqueConstraint(name = "uk_gift_assignments_recipient", columnNames = {"event_id", "recipient_id"})
})
public class GiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private GiftEvent event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giver_id", nullable = false)
    private Participant giver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Participant recipient;

    protected GiftAssignment() {
    }

    GiftAssignment(GiftEvent event, Participant giver, Participant recipient) {
        this.event = event;
        this.giver = giver;
        this.recipient = recipient;
    }
}
