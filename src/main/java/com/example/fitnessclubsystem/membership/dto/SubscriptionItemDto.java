package com.example.fitnessclubsystem.membership.dto;

import java.util.UUID;

public record SubscriptionItemDto(
        UUID id,
        UUID clientId,
        UUID planId,
        String planName,
        String startDate,
        String endDate,
        Integer remainingVisits,
        String status
) {
}