package com.example.fitnessclubsystem.identity.dto;

import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String email,
        String role,
        ClientProfileDto client,
        TrainerProfileDto trainer
) {
}