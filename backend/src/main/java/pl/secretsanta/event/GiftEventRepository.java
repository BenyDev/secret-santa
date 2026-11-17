package pl.secretsanta.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GiftEventRepository extends JpaRepository<GiftEvent, UUID> {
}
