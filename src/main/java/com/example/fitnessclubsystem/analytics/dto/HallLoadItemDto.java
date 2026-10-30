package com.example.fitnessclubsystem.analytics.dto;

import java.util.UUID;

public record HallLoadItemDto(
        UUID hallId,
        String statDate,
        Integer totalSlots,
        Integer bookedSlots,
        Integer attendedCount
) {
}