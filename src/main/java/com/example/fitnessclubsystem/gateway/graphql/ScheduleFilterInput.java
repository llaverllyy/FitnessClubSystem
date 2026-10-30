package com.example.fitnessclubsystem.gateway.graphql;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleFilterInput(
        UUID trainerId,
        UUID categoryId,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        String status
) {
}