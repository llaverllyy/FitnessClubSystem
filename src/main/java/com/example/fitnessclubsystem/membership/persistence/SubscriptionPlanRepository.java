package com.example.fitnessclubsystem.membership.persistence;

import com.example.fitnessclubsystem.membership.domain.SubscriptionPlanEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, UUID> {
}