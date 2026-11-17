package pl.secretsanta.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWishlistRequest(
        @NotNull @Size(max = 2000) @Pattern(regexp = "[^\\x00]*") String wishlist
) {
}
