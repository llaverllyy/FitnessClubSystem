package com.example.fitnessclubsystem.analytics.dto;

import java.util.UUID;

public record ActiveClientItemDto(
        UUID clientId,
        Integer bookingsCount,
        Integer attendanceCount,
        Integer cancellationsCount
) {
}