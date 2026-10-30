package com.example.fitnessclubsystem.membership.persistence;

import com.example.fitnessclubsystem.membership.domain.SubscriptionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    @EntityGraph(attributePaths = "plan")
    List<SubscriptionEntity> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @EntityGraph(attributePaths = "plan")
    @Query("select s from SubscriptionEntity s where s.id = :id")
    Optional<SubscriptionEntity> findWithPlanById(@Param("id") UUID id);
}