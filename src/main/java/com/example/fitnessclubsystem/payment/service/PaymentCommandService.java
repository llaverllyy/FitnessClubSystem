package com.example.fitnessclubsystem.payment.service;

import com.example.fitnessclubsystem.integration.membership.MembershipClient;
import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import com.example.fitnessclubsystem.membership.service.MembershipCommandService;
import com.example.fitnessclubsystem.membership.service.MembershipQueryService;
import com.example.fitnessclubsystem.notification.service.NotificationCommandService;
import com.example.fitnessclubsystem.payment.domain.PaymentEntity;
import com.example.fitnessclubsystem.payment.dto.PaymentItemDto;
import com.example.fitnessclubsystem.payment.dto.SubscriptionPurchaseDto;
import com.example.fitnessclubsystem.payment.persistence.PaymentRepository;
import com.example.fitnessclubsystem.shared.exception.BusinessRuleException;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentQueryService paymentQueryService;
    private final MembershipCommandService membershipCommandService;
    private final MembershipQueryService membershipQueryService;
    private final MembershipClient membershipClient;
    private final NotificationCommandService notificationCommandService;

    public PaymentCommandService(
            PaymentRepository paymentRepository,
            PaymentQueryService paymentQueryService,
            MembershipCommandService membershipCommandService,
            MembershipQueryService membershipQueryService,
            MembershipClient membershipClient,
            NotificationCommandService notificationCommandService) {
        this.paymentRepository = paymentRepository;
        this.paymentQueryService = paymentQueryService;
        this.membershipCommandService = membershipCommandService;
        this.membershipQueryService = membershipQueryService;
        this.membershipClient = membershipClient;
        this.notificationCommandService = notificationCommandService;
    }

    public SubscriptionPurchaseDto purchaseSubscription(UUID clientId, UUID planId, String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new BusinessRuleException("paymentMethod is required");
        }
        SubscriptionItemDto subscription = membershipCommandService.createPending(clientId, planId);
        var plan = membershipQueryService.findPlans(false).stream()
                .filter(p -> p.id().equals(planId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("SubscriptionPlan", planId));

        PaymentEntity payment = new PaymentEntity();
        payment.setId(UUID.randomUUID());
        payment.setClientId(clientId);
        payment.setSubscriptionId(subscription.id());
        payment.setAmount(BigDecimal.valueOf(plan.price()));
        payment.setCurrency("RUB");
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("PENDING");
        payment.setCreatedAt(Instant.now());
        paymentRepository.save(payment);

        return new SubscriptionPurchaseDto(subscription, paymentQueryService.getById(payment.getId()));
    }

    public PaymentItemDto confirmPayment(UUID paymentId, String status) {
        if (!"COMPLETED".equals(status) && !"FAILED".equals(status)) {
            throw new BusinessRuleException("Payment status must be COMPLETED or FAILED");
        }
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment", paymentId));
        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessRuleException("Only PENDING payment can be confirmed");
        }
        payment.setStatus(status);
        if ("COMPLETED".equals(status)) {
            payment.setPaidAt(Instant.now());
            membershipClient.activateSubscription(payment.getSubscriptionId(), payment.getId());
            notificationCommandService.notifyPaymentCompleted(payment.getClientId(), payment.getId());
        } else {
            notificationCommandService.notifyPaymentFailed(payment.getClientId(), payment.getId());
        }
        paymentRepository.save(payment);
        return paymentQueryService.getById(paymentId);
    }
}
