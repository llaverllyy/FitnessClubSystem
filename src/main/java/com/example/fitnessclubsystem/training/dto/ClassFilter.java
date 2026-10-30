package com.example.fitnessclubsystem.training.dto;

import java.util.UUID;

public record ClassFilter(
        UUID categoryId,
        UUID trainerId,
        String status,
        String titleContains,
        String sortBy,
        String sortDirection
) {
}
