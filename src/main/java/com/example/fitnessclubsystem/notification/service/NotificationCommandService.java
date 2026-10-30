package com.example.fitnessclubsystem.notification.service;

import com.example.fitnessclubsystem.identity.service.IdentityQueryService;
import com.example.fitnessclubsystem.notification.domain.NotificationEntity;
import com.example.fitnessclubsystem.notification.persistence.NotificationRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final IdentityQueryService identityQueryService;

    public NotificationCommandService(
            NotificationRepository notificationRepository,
            IdentityQueryService identityQueryService) {
        this.notificationRepository = notificationRepository;
        this.identityQueryService = identityQueryService;
    }

    public void notifyUserRegistered(UUID userId) {
        save(userId, "USER_REGISTERED", "Добро пожаловать", "Регистрация успешно завершена", "USER", userId);
    }

    public void notifyBookingCreated(UUID clientId, UUID bookingId) {
        UUID userId = identityQueryService.getUserIdByClientId(clientId);
        save(userId, "BOOKING_CREATED", "Запись подтверждена", "Вы успешно записаны на занятие", "BOOKING", bookingId);
    }

    public void notifyBookingCancelled(UUID clientId, UUID bookingId) {
        UUID userId = identityQueryService.getUserIdByClientId(clientId);
        save(userId, "BOOKING_CANCELLED", "Запись отменена", "Ваша запись на занятие отменена", "BOOKING", bookingId);
    }

    public void notifyScheduleCancelled(UUID clientId, UUID bookingId) {
        UUID userId = identityQueryService.getUserIdByClientId(clientId);
        save(userId, "SCHEDULE_CANCELLED", "Занятие отменено", "Занятие клубом отменено, запись снята", "BOOKING", bookingId);
    }

    public void notifyPaymentCompleted(UUID clientId, UUID paymentId) {
        UUID userId = identityQueryService.getUserIdByClientId(clientId);
        save(userId, "PAYMENT_COMPLETED", "Оплата успешна", "Платёж подтверждён, абонемент будет активирован", "PAYMENT", paymentId);
    }

    public void notifyPaymentFailed(UUID clientId, UUID paymentId) {
        UUID userId = identityQueryService.getUserIdByClientId(clientId);
        save(userId, "PAYMENT_FAILED", "Оплата не прошла", "Платёж завершился ошибкой", "PAYMENT", paymentId);
    }

    private void save(
            UUID userId,
            String type,
            String title,
            String message,
            String relatedType,
            UUID relatedId) {
        Instant now = Instant.now();
        NotificationEntity entity = new NotificationEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setChannel("IN_APP");
        entity.setTitle(title);
        entity.setMessage(message);
        entity.setStatus("SENT");
        entity.setRelatedEntityType(relatedType);
        entity.setRelatedEntityId(relatedId);
        entity.setCreatedAt(now);
        entity.setSentAt(now);
        notificationRepository.save(entity);
    }
}
