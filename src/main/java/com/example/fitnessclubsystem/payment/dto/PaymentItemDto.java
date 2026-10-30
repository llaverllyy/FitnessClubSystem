package com.example.fitnessclubsystem.payment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentItemDto(
        UUID id,
        UUID clientId,
        UUID subscriptionId,
        Double amount,
        String currency,
        String paymentMethod,
        String status,
        OffsetDateTime paidAt
) {
}