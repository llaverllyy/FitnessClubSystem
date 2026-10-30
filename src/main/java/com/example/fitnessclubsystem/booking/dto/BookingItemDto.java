package com.example.fitnessclubsystem.booking.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingItemDto(
        UUID id,
        UUID clientId,
        UUID scheduleId,
        UUID subscriptionId,
        String status,
        OffsetDateTime bookedAt,
        OffsetDateTime cancelledAt,
        OffsetDateTime attendanceMarkedAt,
        OffsetDateTime scheduleStartTime,
        String classTitle
) {
}