package com.example.fitnessclubsystem.booking.persistence;

import com.example.fitnessclubsystem.booking.domain.BookingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    List<BookingEntity> findByClientIdOrderByBookedAtDesc(UUID clientId);

    List<BookingEntity> findByClientIdAndStatusOrderByBookedAtDesc(UUID clientId, String status);

    List<BookingEntity> findByScheduleIdAndStatus(UUID scheduleId, String status);

    Optional<BookingEntity> findByClientIdAndScheduleIdAndStatus(UUID clientId, UUID scheduleId, String status);
}
