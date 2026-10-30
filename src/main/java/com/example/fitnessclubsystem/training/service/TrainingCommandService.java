package com.example.fitnessclubsystem.training.service;

import com.example.fitnessclubsystem.integration.booking.BookingClient;
import com.example.fitnessclubsystem.integration.identity.IdentityClient;
import com.example.fitnessclubsystem.shared.exception.BusinessRuleException;
import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.domain.ClassCategoryEntity;
import com.example.fitnessclubsystem.training.domain.ClassEntity;
import com.example.fitnessclubsystem.training.domain.HallEntity;
import com.example.fitnessclubsystem.training.domain.ScheduleEntity;
import com.example.fitnessclubsystem.training.dto.ClassItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.persistence.ClassCategoryRepository;
import com.example.fitnessclubsystem.training.persistence.ClassRepository;
import com.example.fitnessclubsystem.training.persistence.HallRepository;
import com.example.fitnessclubsystem.training.persistence.ScheduleRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrainingCommandService {

    private final ClassCategoryRepository classCategoryRepository;
    private final ClassRepository classRepository;
    private final HallRepository hallRepository;
    private final ScheduleRepository scheduleRepository;
    private final IdentityClient identityClient;
    private final BookingClient bookingClient;
    private final TrainingQueryService trainingQueryService;

    public TrainingCommandService(
            ClassCategoryRepository classCategoryRepository,
            ClassRepository classRepository,
            HallRepository hallRepository,
            ScheduleRepository scheduleRepository,
            IdentityClient identityClient,
            @Lazy BookingClient bookingClient,
            TrainingQueryService trainingQueryService) {
        this.classCategoryRepository = classCategoryRepository;
        this.classRepository = classRepository;
        this.hallRepository = hallRepository;
        this.scheduleRepository = scheduleRepository;
        this.identityClient = identityClient;
        this.bookingClient = bookingClient;
        this.trainingQueryService = trainingQueryService;
    }

    public ClassItemDto createClass(
            String title,
            String description,
            int durationMinutes,
            int capacity,
            UUID categoryId,
            UUID trainerId) {
        if (durationMinutes <= 0 || capacity <= 0) {
            throw new BusinessRuleException("Duration and capacity must be positive");
        }
        identityClient.requireActiveTrainer(trainerId);
        ClassCategoryEntity category = classCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("ClassCategory", categoryId));

        Instant now = Instant.now();
        ClassEntity entity = new ClassEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setDurationMinutes(durationMinutes);
        entity.setCapacity(capacity);
        entity.setCategory(category);
        entity.setTrainerId(trainerId);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        classRepository.save(entity);
        return trainingQueryService.getClassById(entity.getId());
    }

    public ClassItemDto updateClass(
            UUID id,
            String title,
            String description,
            Integer durationMinutes,
            Integer capacity,
            UUID categoryId,
            UUID trainerId,
            String status) {
        ClassEntity entity = classRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NotFoundException("Class", id));
        if (title != null) {
            entity.setTitle(title);
        }
        if (description != null) {
            entity.setDescription(description);
        }
        if (durationMinutes != null) {
            if (durationMinutes <= 0) {
                throw new BusinessRuleException("Duration must be positive");
            }
            entity.setDurationMinutes(durationMinutes);
        }
        if (capacity != null) {
            if (capacity <= 0) {
                throw new BusinessRuleException("Capacity must be positive");
            }
            entity.setCapacity(capacity);
        }
        if (categoryId != null) {
            ClassCategoryEntity category = classCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("ClassCategory", categoryId));
            entity.setCategory(category);
        }
        if (trainerId != null) {
            identityClient.requireActiveTrainer(trainerId);
            entity.setTrainerId(trainerId);
        }
        if (status != null) {
            entity.setStatus(status);
        }
        entity.setUpdatedAt(Instant.now());
        classRepository.save(entity);
        return trainingQueryService.getClassById(entity.getId());
    }

    public ScheduleItemDto createSchedule(
            UUID classId,
            UUID hallId,
            UUID trainerId,
            OffsetDateTime startTime,
            OffsetDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessRuleException("endTime must be after startTime");
        }
        ClassEntity classEntity = classRepository.findWithCategoryById(classId)
                .orElseThrow(() -> new NotFoundException("Class", classId));
        if (!"ACTIVE".equals(classEntity.getStatus())) {
            throw new BusinessRuleException("Class is not active");
        }
        HallEntity hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new NotFoundException("Hall", hallId));
        if (!"ACTIVE".equals(hall.getStatus())) {
            throw new BusinessRuleException("Hall is not active");
        }
        identityClient.requireActiveTrainer(trainerId);

        Instant now = Instant.now();
        ScheduleEntity entity = new ScheduleEntity();
        entity.setId(UUID.randomUUID());
        entity.setClassEntity(classEntity);
        entity.setHall(hall);
        entity.setTrainerId(trainerId);
        entity.setStartTime(startTime.toInstant());
        entity.setEndTime(endTime.toInstant());
        entity.setStatus("PLANNED");
        entity.setAvailableSlots(classEntity.getCapacity());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        scheduleRepository.save(entity);
        return trainingQueryService.getScheduleById(entity.getId());
    }

    public ScheduleItemDto changeStatus(UUID scheduleId, String status) {
        ScheduleEntity entity = scheduleRepository.findDetailedById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule", scheduleId));
        if ("CANCELLED".equals(entity.getStatus()) && !"CANCELLED".equals(status)) {
            throw new BusinessRuleException("Cancelled schedule cannot change status");
        }
        entity.setStatus(status);
        entity.setUpdatedAt(Instant.now());
        scheduleRepository.save(entity);
        if ("CANCELLED".equals(status)) {
            bookingClient.cancelBySchedule(scheduleId);
        }
        return trainingQueryService.getScheduleById(scheduleId);
    }

    public ScheduleItemDto requireAvailableForBooking(UUID scheduleId) {
        ScheduleEntity entity = scheduleRepository.findDetailedById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule", scheduleId));
        if (!"PLANNED".equals(entity.getStatus())) {
            throw new BusinessRuleException("Schedule is not available for booking");
        }
        if (entity.getAvailableSlots() <= 0) {
            throw new BusinessRuleException("No available slots");
        }
        if (entity.getStartTime().isBefore(Instant.now())) {
            throw new BusinessRuleException("Cannot book past schedule");
        }
        return trainingQueryService.getScheduleById(scheduleId);
    }

    public ScheduleItemDto reserveSlot(UUID scheduleId) {
        ScheduleEntity entity = scheduleRepository.findDetailedById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule", scheduleId));
        if (!"PLANNED".equals(entity.getStatus())) {
            throw new BusinessRuleException("Schedule is not available for booking");
        }
        if (entity.getAvailableSlots() <= 0) {
            throw new BusinessRuleException("No available slots");
        }
        entity.setAvailableSlots(entity.getAvailableSlots() - 1);
        entity.setUpdatedAt(Instant.now());
        scheduleRepository.save(entity);
        return trainingQueryService.getScheduleById(scheduleId);
    }

    public ScheduleItemDto releaseSlot(UUID scheduleId) {
        ScheduleEntity entity = scheduleRepository.findDetailedById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule", scheduleId));
        int capacity = entity.getClassEntity().getCapacity();
        if (entity.getAvailableSlots() >= capacity) {
            return trainingQueryService.getScheduleById(scheduleId);
        }
        entity.setAvailableSlots(entity.getAvailableSlots() + 1);
        entity.setUpdatedAt(Instant.now());
        scheduleRepository.save(entity);
        return trainingQueryService.getScheduleById(scheduleId);
    }
}
