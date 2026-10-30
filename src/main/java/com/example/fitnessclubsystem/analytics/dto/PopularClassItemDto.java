package com.example.fitnessclubsystem.analytics.dto;

import java.util.UUID;

public record PopularClassItemDto(
        UUID classId,
        Integer bookingsCount,
        Integer cancellationsCount,
        Integer attendanceCount
) {
}