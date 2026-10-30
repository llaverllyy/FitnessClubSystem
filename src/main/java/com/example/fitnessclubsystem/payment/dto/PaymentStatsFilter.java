package com.example.fitnessclubsystem.payment.dto;

import java.util.UUID;

public record PaymentStatsFilter(
        UUID clientId,
        String status
) {
}
