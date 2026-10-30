package com.example.fitnessclubsystem.training.service;

import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.domain.ClassEntity;
import com.example.fitnessclubsystem.training.domain.ScheduleEntity;
import com.example.fitnessclubsystem.training.dto.ClassItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleFilter;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.persistence.ClassRepository;
import com.example.fitnessclubsystem.training.persistence.ScheduleRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TrainingQueryService {

    private final ClassRepository classRepository;
    private final ScheduleRepository scheduleRepository;

    public TrainingQueryService(ClassRepository classRepository, ScheduleRepository scheduleRepository) {
        this.classRepository = classRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public ClassItemDto getClassById(UUID id) {
        return classRepository.findWithCategoryById(id)
                .map(this::toClassDto)
                .orElseThrow(() -> new NotFoundException("Class", id));
    }

    public ScheduleItemDto getScheduleById(UUID id) {
        return scheduleRepository.findDetailedById(id)
                .map(this::toScheduleDto)
                .orElseThrow(() -> new NotFoundException("Schedule", id));
    }

    public List<ScheduleItemDto> findSchedules(ScheduleFilter filter) {
        ScheduleFilter safe = filter == null
                ? new ScheduleFilter(null, null, null, null, null)
                : filter;
        return scheduleRepository.findByFilter(
                        safe.trainerId(),
                        safe.categoryId(),
                        safe.status(),
                        toInstant(safe.startDate()),
                        toInstant(safe.endDate()))
                .stream()
                .map(this::toScheduleDto)
                .toList();
    }

    public List<ScheduleItemDto> findTrainerSchedule(UUID trainerId, OffsetDateTime from, OffsetDateTime to) {
        return scheduleRepository.findTrainerSchedule(trainerId, toInstant(from), toInstant(to)).stream()
                .map(this::toScheduleDto)
                .toList();
    }

    private ClassItemDto toClassDto(ClassEntity entity) {
        return new ClassItemDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                entity.getCapacity(),
                entity.getCategory().getId(),
                entity.getTrainerId(),
                entity.getStatus()
        );
    }

    private ScheduleItemDto toScheduleDto(ScheduleEntity entity) {
        return new ScheduleItemDto(
                entity.getId(),
                entity.getClassEntity().getId(),
                entity.getHall().getId(),
                entity.getTrainerId(),
                toOffset(entity.getStartTime()),
                toOffset(entity.getEndTime()),
                entity.getStatus(),
                entity.getAvailableSlots(),
                toClassDto(entity.getClassEntity()),
                entity.getHall().getName()
        );
    }

    private Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private OffsetDateTime toOffset(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}