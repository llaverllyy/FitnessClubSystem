package com.example.fitnessclubsystem.identity.persistence;

import com.example.fitnessclubsystem.identity.domain.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {

    Optional<ClientEntity> findByUser_Id(UUID userId);
}