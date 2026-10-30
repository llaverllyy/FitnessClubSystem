package com.example.fitnessclubsystem.integration.membership;

import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import com.example.fitnessclubsystem.membership.service.MembershipQueryService;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MembershipClientStub implements MembershipClient {

    private final MembershipQueryService membershipQueryService;

    public MembershipClientStub(MembershipQueryService membershipQueryService) {
        this.membershipQueryService = membershipQueryService;
    }

    @Override
    public Optional<SubscriptionItemDto> findSubscriptionById(UUID subscriptionId) {
        try {
            return Optional.of(membershipQueryService.getById(subscriptionId));
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
    }
}