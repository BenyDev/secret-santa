package pl.secretsanta.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public record RegisterRequest(
        @NotBlank(message = "Podaj adres e-mail.")
        @Email(message = "Podaj poprawny adres e-mail.")
        @Size(max = 254, message = "Adres e-mail może mieć maksymalnie 254 znaki.")
        String email,

        @NotBlank(message = "Podaj hasło.")
        @Size(min = 8, message = "Hasło musi mieć co najmniej 8 znaków.")
        String password
) {
    public RegisterRequest {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    @AssertTrue(message = "Hasło może mieć maksymalnie 72 bajty w UTF-8.")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "RegisterRequest[redacted]";
    }
}
