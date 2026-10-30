package com.example.fitnessclubsystem.notification.service;

import com.example.fitnessclubsystem.notification.domain.NotificationEntity;
import com.example.fitnessclubsystem.notification.dto.NotificationItemDto;
import com.example.fitnessclubsystem.notification.persistence.NotificationRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationItemDto> findByUser(UUID userId, boolean unreadOnly) {
        List<NotificationEntity> items = unreadOnly
                ? notificationRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, "READ")
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return items.stream().map(this::toDto).toList();
    }

    private NotificationItemDto toDto(NotificationEntity entity) {
        return new NotificationItemDto(
                entity.getId(),
                entity.getType(),
                entity.getChannel(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getStatus(),
                toOffset(entity.getCreatedAt()),
                toOffset(entity.getSentAt())
        );
    }

    private OffsetDateTime toOffset(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}