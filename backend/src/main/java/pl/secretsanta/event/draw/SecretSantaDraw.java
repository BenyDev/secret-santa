package pl.secretsanta.event.draw;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Pure draw algorithm: a shuffled cycle gives every person one distinct recipient. */
public final class SecretSantaDraw {
    private final SecureRandom random;

    public SecretSantaDraw(SecureRandom random) {
        this.random = Objects.requireNonNull(random);
    }

    public List<DrawPair> draw(List<UUID> participantIds) {
        Objects.requireNonNull(participantIds);
        if (participantIds.size() < 3 || new HashSet<>(participantIds).size() != participantIds.size()
                || participantIds.contains(null)) {
            throw new IllegalArgumentException("At least three distinct participants are required");
        }
        List<UUID> shuffled = new ArrayList<>(participantIds);
        Collections.shuffle(shuffled, random);
        List<DrawPair> result = new ArrayList<>(shuffled.size());
        for (int index = 0; index < shuffled.size(); index++) {
            result.add(new DrawPair(shuffled.get(index), shuffled.get((index + 1) % shuffled.size())));
        }
        return List.copyOf(result);
    }
}
