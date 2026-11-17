package pl.secretsanta.event;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateGiftEventRequest(
        @NotBlank(message = "Podaj nazwę wydarzenia.")
        @Size(max = 120, message = "Nazwa może mieć maksymalnie 120 znaków.")
        String name,
        @NotNull(message = "Podaj budżet.")
        @DecimalMin(value = "0", inclusive = false, message = "Budżet musi być większy od zera.")
        @Digits(integer = 8, fraction = 2, message = "Budżet może mieć do 8 cyfr przed przecinkiem i 2 po przecinku.")
        BigDecimal budget,
        @NotBlank(message = "Podaj pseudonim organizatora.")
        @Size(max = 60, message = "Pseudonim może mieć maksymalnie 60 znaków.")
        String organizerNickname
) {
    public CreateGiftEventRequest {
        if (name != null) {
            name = name.strip();
        }
        if (organizerNickname != null) {
            organizerNickname = organizerNickname.strip();
        }
    }
}
