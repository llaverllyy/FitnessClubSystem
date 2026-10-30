package com.example.fitnessclubsystem.training.dto;

import java.util.UUID;

public record ClassItemDto(
        UUID id,
        String title,
        String description,
        Integer durationMinutes,
        Integer capacity,
        UUID categoryId,
        UUID trainerId,
        String status
) {
}