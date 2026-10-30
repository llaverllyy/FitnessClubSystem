package com.example.fitnessclubsystem.identity.persistence;

import com.example.fitnessclubsystem.identity.domain.TrainerEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<TrainerEntity, UUID> {

    Optional<TrainerEntity> findByUser_Id(UUID userId);
}