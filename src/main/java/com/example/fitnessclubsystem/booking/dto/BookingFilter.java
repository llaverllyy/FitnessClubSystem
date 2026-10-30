package com.example.fitnessclubsystem.booking.dto;

import java.util.UUID;

public record BookingFilter(
        UUID clientId,
        UUID scheduleId,
        String status,
        String sortBy,
        String sortDirection
) {
}
