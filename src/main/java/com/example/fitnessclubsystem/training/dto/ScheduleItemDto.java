package com.example.fitnessclubsystem.training.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleItemDto(
        UUID id,
        UUID classId,
        UUID hallId,
        UUID trainerId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String status,
        Integer availableSlots,
        ClassItemDto classItem,
        String hallName
) {
}