package com.example.fitnessclubsystem.membership.service;

import com.example.fitnessclubsystem.membership.domain.SubscriptionEntity;
import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
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

    public MembershipQueryService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
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