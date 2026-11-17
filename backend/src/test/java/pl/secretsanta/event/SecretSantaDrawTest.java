package pl.secretsanta.event;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.secretsanta.event.draw.SecretSantaDraw;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SecretSantaDrawTest {
    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 10, 100})
    void producesOneDifferentRecipientAndOneAssignmentPerPerson(int count) {
        List<UUID> people = IntStream.range(0, count).mapToObj(index -> UUID.randomUUID()).toList();
        SecretSantaDraw algorithm = new SecretSantaDraw(new SecureRandom());

        for (int attempt = 0; attempt < 20; attempt++) {
            var assignments = algorithm.draw(people);
            assertThat(assignments).hasSize(count);
            assertThat(new HashSet<>(assignments.stream().map(pair -> pair.giverId()).toList()))
                    .containsExactlyInAnyOrderElementsOf(people);
            assertThat(new HashSet<>(assignments.stream().map(pair -> pair.recipientId()).toList()))
                    .containsExactlyInAnyOrderElementsOf(people);
            assertThat(assignments).allSatisfy(pair -> assertThat(pair.giverId()).isNotEqualTo(pair.recipientId()));
        }
    }
}
