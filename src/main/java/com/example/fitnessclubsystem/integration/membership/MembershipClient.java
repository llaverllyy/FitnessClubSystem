package com.example.fitnessclubsystem.integration.membership;

import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import java.util.Optional;
import java.util.UUID;

public interface MembershipClient {

    Optional<SubscriptionItemDto> findSubscriptionById(UUID subscriptionId);

    SubscriptionItemDto requireValidSubscription(UUID subscriptionId, UUID clientId);

    SubscriptionItemDto activateSubscription(UUID subscriptionId, UUID paymentId);

    SubscriptionItemDto registerVisit(UUID subscriptionId);
}
