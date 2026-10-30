package com.example.fitnessclubsystem.training.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleFilter(
        UUID trainerId,
        UUID categoryId,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        String status,
        String sortBy,
        String sortDirection
) {
    public ScheduleFilter(
            UUID trainerId,
            UUID categoryId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String status) {
        this(trainerId, categoryId, startDate, endDate, status, null, null);
    }
}
