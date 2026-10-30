package com.example.fitnessclubsystem.membership.service;

import com.example.fitnessclubsystem.membership.domain.SubscriptionEntity;
import com.example.fitnessclubsystem.membership.domain.SubscriptionPlanEntity;
import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import com.example.fitnessclubsystem.membership.persistence.SubscriptionPlanRepository;
import com.example.fitnessclubsystem.membership.persistence.SubscriptionRepository;
import com.example.fitnessclubsystem.shared.exception.BusinessRuleException;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipCommandService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MembershipQueryService membershipQueryService;

    public MembershipCommandService(
            SubscriptionPlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            MembershipQueryService membershipQueryService) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.membershipQueryService = membershipQueryService;
    }

    public SubscriptionItemDto createPending(UUID clientId, UUID planId) {
        SubscriptionPlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("SubscriptionPlan", planId));
        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new BusinessRuleException("Subscription plan is not active");
        }

        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setClientId(clientId);
        entity.setPlan(plan);
        entity.setRemainingVisits(plan.getVisitLimit());
        entity.setStatus("PENDING");
        entity.setCreatedAt(Instant.now());
        subscriptionRepository.save(entity);
        return membershipQueryService.getById(entity.getId());
    }

    public SubscriptionItemDto requireValidForBooking(UUID subscriptionId, UUID clientId) {
        SubscriptionEntity entity = subscriptionRepository.findWithPlanById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription", subscriptionId));
        if (!entity.getClientId().equals(clientId)) {
            throw new BusinessRuleException("Subscription does not belong to client");
        }
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessRuleException("Subscription is not active");
        }
        if (entity.getEndDate() != null && entity.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Subscription expired");
        }
        if (entity.getRemainingVisits() <= 0) {
            throw new BusinessRuleException("No remaining visits");
        }
        return membershipQueryService.getById(subscriptionId);
    }

    public SubscriptionItemDto activate(UUID subscriptionId, UUID paymentId) {
        SubscriptionEntity entity = subscriptionRepository.findWithPlanById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription", subscriptionId));
        if ("ACTIVE".equals(entity.getStatus())) {
            return membershipQueryService.getById(subscriptionId);
        }
        if (!"PENDING".equals(entity.getStatus())) {
            throw new BusinessRuleException("Only PENDING subscription can be activated");
        }
        LocalDate start = LocalDate.now();
        entity.setStatus("ACTIVE");
        entity.setStartDate(start);
        entity.setEndDate(start.plusDays(entity.getPlan().getDurationDays()));
        entity.setRemainingVisits(entity.getPlan().getVisitLimit());
        entity.setActivatedAt(Instant.now());
        subscriptionRepository.save(entity);
        return membershipQueryService.getById(subscriptionId);
    }

    public SubscriptionItemDto registerVisit(UUID subscriptionId) {
        SubscriptionEntity entity = subscriptionRepository.findWithPlanById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription", subscriptionId));
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessRuleException("Subscription is not active");
        }
        if (entity.getRemainingVisits() <= 0) {
            throw new BusinessRuleException("No remaining visits");
        }
        entity.setRemainingVisits(entity.getRemainingVisits() - 1);
        subscriptionRepository.save(entity);
        return membershipQueryService.getById(subscriptionId);
    }
}
