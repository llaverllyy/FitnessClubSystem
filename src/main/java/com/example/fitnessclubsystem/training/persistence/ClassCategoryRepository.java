package com.example.fitnessclubsystem.training.persistence;

import com.example.fitnessclubsystem.training.domain.ClassCategoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassCategoryRepository extends JpaRepository<ClassCategoryEntity, UUID> {
}