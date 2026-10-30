package com.example.fitnessclubsystem.training.persistence;

import com.example.fitnessclubsystem.training.domain.HallEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<HallEntity, UUID> {
}