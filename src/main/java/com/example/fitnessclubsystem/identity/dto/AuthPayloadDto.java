package com.example.fitnessclubsystem.identity.dto;

public record AuthPayloadDto(
        String accessToken,
        UserProfileDto user
) {
}
