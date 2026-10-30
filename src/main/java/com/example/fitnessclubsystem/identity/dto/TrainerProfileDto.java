package com.example.fitnessclubsystem.identity.dto;

import java.util.UUID;

public record TrainerProfileDto(
        UUID id,
        String fullName,
        String phone,
        String specialization,
        String employmentStatus
) {
}