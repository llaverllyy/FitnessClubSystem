package com.example.fitnessclubsystem.payment.persistence;

import com.example.fitnessclubsystem.payment.domain.PaymentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    List<PaymentEntity> findByClientIdOrderByCreatedAtDesc(UUID clientId);
}