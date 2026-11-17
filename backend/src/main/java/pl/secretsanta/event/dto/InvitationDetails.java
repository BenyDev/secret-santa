package pl.secretsanta.event.dto;

import java.math.BigDecimal;

public record InvitationDetails(String name, BigDecimal budget, boolean canJoin) {
}
