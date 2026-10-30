package com.example.fitnessclubsystem.identity.dto;

import java.util.UUID;

public record ClientProfileDto(
        UUID id,
        String fullName,
        String phone,
        String membershipStatus
) {
}