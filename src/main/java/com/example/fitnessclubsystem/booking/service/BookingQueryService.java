package com.example.fitnessclubsystem.booking.service;

import com.example.fitnessclubsystem.booking.domain.BookingEntity;
import com.example.fitnessclubsystem.booking.dto.BookingFilter;
import com.example.fitnessclubsystem.booking.dto.BookingItemDto;
import com.example.fitnessclubsystem.booking.dto.BookingStatsDto;
import com.example.fitnessclubsystem.booking.persistence.BookingRepository;
import com.example.fitnessclubsystem.integration.training.TrainingClient;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
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

    public List<BookingItemDto> findByClient(UUID clientId, String status, String sortBy, String sortDirection) {
        List<BookingEntity> bookings = status == null
                ? bookingRepository.findByClientIdOrderByBookedAtDesc(clientId)
                : bookingRepository.findByClientIdAndStatusOrderByBookedAtDesc(clientId, status);
        return sort(bookings.stream().map(this::toDto).toList(), sortBy, sortDirection);
    }

    public List<BookingItemDto> find(BookingFilter filter) {
        BookingFilter safe = filter == null
                ? new BookingFilter(null, null, null, null, null)
                : filter;
        return sort(bookingRepository.findAll().stream()
                .filter(b -> safe.clientId() == null || b.getClientId().equals(safe.clientId()))
                .filter(b -> safe.scheduleId() == null || b.getScheduleId().equals(safe.scheduleId()))
                .filter(b -> safe.status() == null || b.getStatus().equals(safe.status()))
                .map(this::toDto)
                .toList(), safe.sortBy(), safe.sortDirection());
    }

    public BookingStatsDto stats(BookingFilter filter) {
        List<BookingItemDto> items = find(filter);
        return new BookingStatsDto(
                items.size(),
                (int) items.stream().filter(i -> "BOOKED".equals(i.status())).count(),
                (int) items.stream()
                        .filter(i -> "CANCELLED".equals(i.status()) || "CANCELLED_BY_CLUB".equals(i.status()))
                        .count(),
                (int) items.stream().filter(i -> "ATTENDED".equals(i.status())).count(),
                (int) items.stream().filter(i -> "NO_SHOW".equals(i.status())).count()
        );
    }

    private List<BookingItemDto> sort(List<BookingItemDto> items, String sortBy, String sortDirection) {
        Comparator<BookingItemDto> comparator = switch (sortBy == null ? "BOOKED_AT" : sortBy) {
            case "STATUS" -> Comparator.comparing(BookingItemDto::status);
            default -> Comparator.comparing(BookingItemDto::bookedAt);
        };
        if (!"ASC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return items.stream().sorted(comparator).toList();
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
