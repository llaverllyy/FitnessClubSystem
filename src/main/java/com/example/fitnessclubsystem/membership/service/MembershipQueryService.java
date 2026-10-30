package com.example.fitnessclubsystem.membership.service;

import com.example.fitnessclubsystem.membership.domain.SubscriptionEntity;
import com.example.fitnessclubsystem.membership.domain.SubscriptionPlanEntity;
import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import com.example.fitnessclubsystem.membership.dto.SubscriptionPlanItemDto;
import com.example.fitnessclubsystem.membership.persistence.SubscriptionPlanRepository;
import com.example.fitnessclubsystem.membership.persistence.SubscriptionRepository;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MembershipQueryService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public MembershipQueryService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public SubscriptionItemDto getById(UUID id) {
        return subscriptionRepository.findWithPlanById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Subscription", id));
    }

    public List<SubscriptionItemDto> findByClientId(UUID clientId) {
        return subscriptionRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<SubscriptionPlanItemDto> findPlans(boolean activeOnly) {
        return subscriptionPlanRepository.findAll().stream()
                .filter(plan -> !activeOnly || Boolean.TRUE.equals(plan.getActive()))
                .map(this::toPlanDto)
                .toList();
    }

    private SubscriptionPlanItemDto toPlanDto(SubscriptionPlanEntity plan) {
        return new SubscriptionPlanItemDto(
                plan.getId(),
                plan.getName(),
                plan.getDurationDays(),
                plan.getVisitLimit(),
                plan.getPrice().doubleValue(),
                plan.getActive()
        );
    }

    private SubscriptionItemDto toDto(SubscriptionEntity entity) {
        return new SubscriptionItemDto(
                entity.getId(),
                entity.getClientId(),
                entity.getPlan().getId(),
                entity.getPlan().getName(),
                entity.getStartDate() == null ? null : entity.getStartDate().toString(),
                entity.getEndDate() == null ? null : entity.getEndDate().toString(),
                entity.getRemainingVisits(),
                entity.getStatus()
        );
    }
}