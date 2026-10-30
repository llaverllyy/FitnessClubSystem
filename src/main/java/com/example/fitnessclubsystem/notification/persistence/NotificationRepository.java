package com.example.fitnessclubsystem.notification.persistence;

import com.example.fitnessclubsystem.notification.domain.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<NotificationEntity> findByUserIdAndStatusNotOrderByCreatedAtDesc(UUID userId, String status);
}