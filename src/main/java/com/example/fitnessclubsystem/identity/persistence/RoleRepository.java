package com.example.fitnessclubsystem.identity.persistence;

import com.example.fitnessclubsystem.identity.domain.RoleEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
}