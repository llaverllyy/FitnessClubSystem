package com.example.fitnessclubsystem.identity.service;

import com.example.fitnessclubsystem.identity.domain.ClientEntity;
import com.example.fitnessclubsystem.identity.domain.TrainerEntity;
import com.example.fitnessclubsystem.identity.domain.UserEntity;
import com.example.fitnessclubsystem.identity.dto.ClientProfileDto;
import com.example.fitnessclubsystem.identity.dto.TrainerProfileDto;
import com.example.fitnessclubsystem.identity.dto.UserProfileDto;
import com.example.fitnessclubsystem.identity.persistence.ClientRepository;
import com.example.fitnessclubsystem.identity.persistence.TrainerRepository;
import com.example.fitnessclubsystem.identity.persistence.UserRepository;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IdentityQueryService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TrainerRepository trainerRepository;

    public IdentityQueryService(
            UserRepository userRepository,
            ClientRepository clientRepository,
            TrainerRepository trainerRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.trainerRepository = trainerRepository;
    }

    public UserProfileDto getUserProfile(UUID userId) {
        UserEntity user = userRepository.findWithRoleById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        ClientProfileDto client = clientRepository.findByUser_Id(userId)
                .map(this::toClientDto)
                .orElse(null);

        TrainerProfileDto trainer = trainerRepository.findByUser_Id(userId)
                .map(this::toTrainerDto)
                .orElse(null);

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getRole().getCode(),
                client,
                trainer
        );
    }

    public TrainerProfileDto getTrainer(UUID trainerId) {
        return trainerRepository.findById(trainerId)
                .map(this::toTrainerDto)
                .orElseThrow(() -> new NotFoundException("Trainer", trainerId));
    }

    public ClientProfileDto getClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(this::toClientDto)
                .orElseThrow(() -> new NotFoundException("Client", clientId));
    }

    public UUID getUserIdByClientId(UUID clientId) {
        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client", clientId));
        return client.getUser().getId();
    }

    private ClientProfileDto toClientDto(ClientEntity entity) {
        return new ClientProfileDto(
                entity.getId(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getMembershipStatus()
        );
    }

    private TrainerProfileDto toTrainerDto(TrainerEntity entity) {
        return new TrainerProfileDto(
                entity.getId(),
                entity.getFullName(),
                entity.getPhone(),
                entity.getSpecialization(),
                entity.getEmploymentStatus()
        );
    }
}