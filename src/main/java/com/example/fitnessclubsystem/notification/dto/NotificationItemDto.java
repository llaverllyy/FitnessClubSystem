package com.example.fitnessclubsystem.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationItemDto(
        UUID id,
        String type,
        String channel,
        String title,
        String message,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime sentAt
) {
}