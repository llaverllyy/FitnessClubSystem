package com.example.fitnessclubsystem.integration.identity;

import com.example.fitnessclubsystem.identity.dto.TrainerProfileDto;
import java.util.Optional;
import java.util.UUID;

public interface IdentityClient {

    Optional<TrainerProfileDto> findTrainerById(UUID trainerId);
}