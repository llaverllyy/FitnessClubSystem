package com.example.fitnessclubsystem.payment.dto;

import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;

public record SubscriptionPurchaseDto(
        SubscriptionItemDto subscription,
        PaymentItemDto payment
) {
}
