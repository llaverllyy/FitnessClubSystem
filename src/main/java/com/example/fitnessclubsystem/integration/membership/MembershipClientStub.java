package com.example.fitnessclubsystem.integration.membership;

import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import com.example.fitnessclubsystem.membership.service.MembershipCommandService;
import com.example.fitnessclubsystem.membership.service.MembershipQueryService;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MembershipClientStub implements MembershipClient {

    private final MembershipQueryService membershipQueryService;
    private final MembershipCommandService membershipCommandService;

    public MembershipClientStub(
            MembershipQueryService membershipQueryService,
            MembershipCommandService membershipCommandService) {
        this.membershipQueryService = membershipQueryService;
        this.membershipCommandService = membershipCommandService;
    }

    @Override
    public Optional<SubscriptionItemDto> findSubscriptionById(UUID subscriptionId) {
        try {
            return Optional.of(membershipQueryService.getById(subscriptionId));
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public SubscriptionItemDto requireValidSubscription(UUID subscriptionId, UUID clientId) {
        return membershipCommandService.requireValidForBooking(subscriptionId, clientId);
    }

    @Override
    public SubscriptionItemDto activateSubscription(UUID subscriptionId, UUID paymentId) {
        return membershipCommandService.activate(subscriptionId, paymentId);
    }

    @Override
    public SubscriptionItemDto registerVisit(UUID subscriptionId) {
        return membershipCommandService.registerVisit(subscriptionId);
    }
}
