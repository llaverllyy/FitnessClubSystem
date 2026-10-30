package com.example.fitnessclubsystem.identity.service;

import com.example.fitnessclubsystem.identity.domain.ClientEntity;
import com.example.fitnessclubsystem.identity.domain.RoleEntity;
import com.example.fitnessclubsystem.identity.domain.TrainerEntity;
import com.example.fitnessclubsystem.identity.domain.UserEntity;
import com.example.fitnessclubsystem.identity.dto.AuthPayloadDto;
import com.example.fitnessclubsystem.identity.persistence.ClientRepository;
import com.example.fitnessclubsystem.identity.persistence.RoleRepository;
import com.example.fitnessclubsystem.identity.persistence.TrainerRepository;
import com.example.fitnessclubsystem.identity.persistence.UserRepository;
import com.example.fitnessclubsystem.notification.service.NotificationCommandService;
import com.example.fitnessclubsystem.shared.exception.BusinessRuleException;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IdentityCommandService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TrainerRepository trainerRepository;
    private final IdentityQueryService identityQueryService;
    private final NotificationCommandService notificationCommandService;

    public IdentityCommandService(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            TrainerRepository trainerRepository,
            IdentityQueryService identityQueryService,
            NotificationCommandService notificationCommandService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.trainerRepository = trainerRepository;
        this.identityQueryService = identityQueryService;
        this.notificationCommandService = notificationCommandService;
    }

    public AuthPayloadDto register(
            String email,
            String password,
            String roleCode,
            String fullName,
            String phone,
            String birthDate,
            String specialization) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new BusinessRuleException("Email and password are required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Email already registered");
        }
        if (!"CLIENT".equals(roleCode) && !"TRAINER".equals(roleCode)) {
            throw new BusinessRuleException("Only CLIENT or TRAINER can self-register");
        }
        if ("TRAINER".equals(roleCode) && (specialization == null || specialization.isBlank())) {
            throw new BusinessRuleException("Trainer specialization is required");
        }

        RoleEntity role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new NotFoundException("Role", roleCode));
        Instant now = Instant.now();

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);

        if ("CLIENT".equals(roleCode)) {
            ClientEntity client = new ClientEntity();
            client.setId(UUID.randomUUID());
            client.setUser(user);
            client.setFullName(fullName);
            client.setPhone(phone);
            client.setBirthDate(birthDate == null || birthDate.isBlank() ? null : LocalDate.parse(birthDate));
            client.setMembershipStatus("NONE");
            client.setCreatedAt(now);
            clientRepository.save(client);
        } else {
            TrainerEntity trainer = new TrainerEntity();
            trainer.setId(UUID.randomUUID());
            trainer.setUser(user);
            trainer.setFullName(fullName);
            trainer.setPhone(phone);
            trainer.setSpecialization(specialization);
            trainer.setEmploymentStatus("ACTIVE");
            trainer.setCreatedAt(now);
            trainerRepository.save(trainer);
        }

        notificationCommandService.notifyUserRegistered(user.getId());
        return new AuthPayloadDto("demo-token-" + user.getId(), identityQueryService.getUserProfile(user.getId()));
    }

    public AuthPayloadDto login(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));
        if (!user.getPasswordHash().equals(password)) {
            throw new BusinessRuleException("Invalid email or password");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessRuleException("User is not active");
        }
        return new AuthPayloadDto("demo-token-" + user.getId(), identityQueryService.getUserProfile(user.getId()));
    }
}
