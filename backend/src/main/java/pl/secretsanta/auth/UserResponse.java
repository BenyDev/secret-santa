package pl.secretsanta.auth;

import java.util.UUID;

public record UserResponse(UUID id, String email) {
}
