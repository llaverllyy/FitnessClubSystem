package com.example.fitnessclubsystem.booking.service;

import com.example.fitnessclubsystem.booking.domain.BookingEntity;
import com.example.fitnessclubsystem.booking.dto.BookingItemDto;
import com.example.fitnessclubsystem.booking.persistence.BookingRepository;
import com.example.fitnessclubsystem.integration.membership.MembershipClient;
import com.example.fitnessclubsystem.integration.training.TrainingClient;
import com.example.fitnessclubsystem.notification.service.NotificationCommandService;
import com.example.fitnessclubsystem.shared.exception.BusinessRuleException;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingCommandService {

    private final BookingRepository bookingRepository;
    private final BookingQueryService bookingQueryService;
    private final TrainingClient trainingClient;
    private final MembershipClient membershipClient;
    private final NotificationCommandService notificationCommandService;

    public BookingCommandService(
            BookingRepository bookingRepository,
            BookingQueryService bookingQueryService,
            @Lazy TrainingClient trainingClient,
            MembershipClient membershipClient,
            NotificationCommandService notificationCommandService) {
        this.bookingRepository = bookingRepository;
        this.bookingQueryService = bookingQueryService;
        this.trainingClient = trainingClient;
        this.membershipClient = membershipClient;
        this.notificationCommandService = notificationCommandService;
    }

    public BookingItemDto book(UUID clientId, UUID scheduleId, UUID subscriptionId) {
        trainingClient.requireAvailableSchedule(scheduleId);
        membershipClient.requireValidSubscription(subscriptionId, clientId);
        bookingRepository.findByClientIdAndScheduleIdAndStatus(clientId, scheduleId, "BOOKED")
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Client already booked this schedule");
                });

        trainingClient.reserveSlot(scheduleId);

        BookingEntity entity = new BookingEntity();
        entity.setId(UUID.randomUUID());
        entity.setClientId(clientId);
        entity.setScheduleId(scheduleId);
        entity.setSubscriptionId(subscriptionId);
        entity.setStatus("BOOKED");
        entity.setBookedAt(Instant.now());
        bookingRepository.save(entity);

        notificationCommandService.notifyBookingCreated(clientId, entity.getId());
        return bookingQueryService.getById(entity.getId());
    }

    public BookingItemDto cancel(UUID bookingId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        if (!"BOOKED".equals(entity.getStatus())) {
            throw new BusinessRuleException("Only BOOKED booking can be cancelled");
        }
        entity.setStatus("CANCELLED");
        entity.setCancelledAt(Instant.now());
        bookingRepository.save(entity);
        trainingClient.releaseSlot(entity.getScheduleId());
        notificationCommandService.notifyBookingCancelled(entity.getClientId(), entity.getId());
        return bookingQueryService.getById(bookingId);
    }

    public BookingItemDto markAttendance(UUID bookingId, String status) {
        if (!"ATTENDED".equals(status) && !"NO_SHOW".equals(status)) {
            throw new BusinessRuleException("Attendance status must be ATTENDED or NO_SHOW");
        }
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        if (!"BOOKED".equals(entity.getStatus())) {
            throw new BusinessRuleException("Attendance can be marked only for BOOKED booking");
        }
        entity.setStatus(status);
        entity.setAttendanceMarkedAt(Instant.now());
        bookingRepository.save(entity);
        if ("ATTENDED".equals(status)) {
            membershipClient.registerVisit(entity.getSubscriptionId());
        }
        return bookingQueryService.getById(bookingId);
    }

    public int cancelByClubForSchedule(UUID scheduleId) {
        List<BookingEntity> bookings = bookingRepository.findByScheduleIdAndStatus(scheduleId, "BOOKED");
        Instant now = Instant.now();
        for (BookingEntity booking : bookings) {
            booking.setStatus("CANCELLED_BY_CLUB");
            booking.setCancelledAt(now);
            bookingRepository.save(booking);
            trainingClient.releaseSlot(scheduleId);
            notificationCommandService.notifyScheduleCancelled(booking.getClientId(), booking.getId());
        }
        return bookings.size();
    }
}
