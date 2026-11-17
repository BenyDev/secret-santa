package pl.secretsanta.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record LoginRequest(
        @NotBlank(message = "Podaj adres e-mail.")
        @Email(message = "Podaj poprawny adres e-mail.")
        @Size(max = 254, message = "Adres e-mail może mieć maksymalnie 254 znaki.")
        String email,
        @NotBlank(message = "Podaj hasło.")
        String password
) {
    public LoginRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public String toString() {
        return "LoginRequest[redacted]";
    }
}
