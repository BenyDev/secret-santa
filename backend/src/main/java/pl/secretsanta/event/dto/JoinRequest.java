package pl.secretsanta.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
        @NotBlank(message = "Podaj pseudonim.")
        @Size(max = 60, message = "Pseudonim może mieć maksymalnie 60 znaków.")
        String nickname
) {
    public JoinRequest {
        if (nickname != null) {
            nickname = nickname.strip();
        }
    }
}
