package com.example.fitnessclubsystem.training.dto;

public record ScheduleStatsDto(
        int totalCount,
        int plannedCount,
        int completedCount,
        int cancelledCount,
        int totalAvailableSlots
) {
}
