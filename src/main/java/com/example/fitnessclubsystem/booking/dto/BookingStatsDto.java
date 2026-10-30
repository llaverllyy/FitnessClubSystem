package com.example.fitnessclubsystem.booking.dto;

public record BookingStatsDto(
        int totalCount,
        int bookedCount,
        int cancelledCount,
        int attendedCount,
        int noShowCount
) {
}
