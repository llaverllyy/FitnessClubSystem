package com.example.fitnessclubsystem.membership.dto;

import java.util.UUID;

public record SubscriptionPlanItemDto(
        UUID id,
        String name,
        Integer durationDays,
        Integer visitLimit,
        Double price,
        Boolean active
) {
}
