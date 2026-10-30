package com.example.fitnessclubsystem.booking.service;

import com.example.fitnessclubsystem.booking.domain.BookingEntity;
import com.example.fitnessclubsystem.booking.dto.BookingItemDto;
import com.example.fitnessclubsystem.booking.persistence.BookingRepository;
import com.example.fitnessclubsystem.integration.training.TrainingClient;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookingQueryService {

    private final BookingRepository bookingRepository;
    private final TrainingClient trainingClient;

    public BookingQueryService(BookingRepository bookingRepository, TrainingClient trainingClient) {
        this.bookingRepository = bookingRepository;
        this.trainingClient = trainingClient;
    }

    public BookingItemDto getById(UUID id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking", id));
        return toDto(booking);
    }

    public List<BookingItemDto> findByClient(UUID clientId, String status) {
        List<BookingEntity> bookings = status == null
                ? bookingRepository.findByClientIdOrderByBookedAtDesc(clientId)
                : bookingRepository.findByClientIdAndStatusOrderByBookedAtDesc(clientId, status);
        return bookings.stream().map(this::toDto).toList();
    }

    private BookingItemDto toDto(BookingEntity entity) {
        ScheduleItemDto schedule = trainingClient.findScheduleById(entity.getScheduleId()).orElse(null);
        return new BookingItemDto(
                entity.getId(),
                entity.getClientId(),
                entity.getScheduleId(),
                entity.getSubscriptionId(),
                entity.getStatus(),
                toOffset(entity.getBookedAt()),
                toOffset(entity.getCancelledAt()),
                toOffset(entity.getAttendanceMarkedAt()),
                schedule == null ? null : schedule.startTime(),
                schedule == null || schedule.classItem() == null ? null : schedule.classItem().title()
        );
    }

    private OffsetDateTime toOffset(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}