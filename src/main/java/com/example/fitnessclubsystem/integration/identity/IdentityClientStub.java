package com.example.fitnessclubsystem.integration.identity;

import com.example.fitnessclubsystem.identity.dto.TrainerProfileDto;
import com.example.fitnessclubsystem.identity.service.IdentityQueryService;
import com.example.fitnessclubsystem.shared.exception.BusinessRuleException;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdentityClientStub implements IdentityClient {

    private final IdentityQueryService identityQueryService;

    public IdentityClientStub(IdentityQueryService identityQueryService) {
        this.identityQueryService = identityQueryService;
    }

    @Override
    public Optional<TrainerProfileDto> findTrainerById(UUID trainerId) {
        try {
            return Optional.of(identityQueryService.getTrainer(trainerId));
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public TrainerProfileDto requireActiveTrainer(UUID trainerId) {
        TrainerProfileDto trainer = identityQueryService.getTrainer(trainerId);
        if (!"ACTIVE".equals(trainer.employmentStatus())) {
            throw new BusinessRuleException("Trainer is not active: " + trainerId);
        }
        return trainer;
    }
}
