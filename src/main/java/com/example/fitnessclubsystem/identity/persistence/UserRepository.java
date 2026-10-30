package com.example.fitnessclubsystem.identity.persistence;

import com.example.fitnessclubsystem.identity.domain.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @EntityGraph(attributePaths = "role")
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findWithRoleById(@Param("id") UUID id);

    @EntityGraph(attributePaths = "role")
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}