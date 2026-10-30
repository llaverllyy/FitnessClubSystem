package com.example.fitnessclubsystem.payment.service;

import com.example.fitnessclubsystem.payment.domain.PaymentEntity;
import com.example.fitnessclubsystem.payment.dto.PaymentItemDto;
import com.example.fitnessclubsystem.payment.persistence.PaymentRepository;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public PaymentQueryService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentItemDto getById(UUID id) {
        return paymentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Payment", id));
    }

    public List<PaymentItemDto> findByClientId(UUID clientId) {
        return paymentRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toDto)
                .toList();
    }

    private PaymentItemDto toDto(PaymentEntity entity) {
        return new PaymentItemDto(
                entity.getId(),
                entity.getClientId(),
                entity.getSubscriptionId(),
                entity.getAmount().doubleValue(),
                entity.getCurrency(),
                entity.getPaymentMethod(),
                entity.getStatus(),
                toOffset(entity.getPaidAt())
        );
    }

    private OffsetDateTime toOffset(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}