package com.example.fitnessclubsystem.training.service;

import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.domain.ClassEntity;
import com.example.fitnessclubsystem.training.domain.ScheduleEntity;
import com.example.fitnessclubsystem.training.dto.ClassFilter;
import com.example.fitnessclubsystem.training.dto.ClassItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleFilter;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleStatsDto;
import com.example.fitnessclubsystem.training.persistence.ClassRepository;
import com.example.fitnessclubsystem.training.persistence.ScheduleRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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

    public List<ClassItemDto> findClasses(ClassFilter filter) {
        ClassFilter safe = filter == null
                ? new ClassFilter(null, null, null, null, null, null)
                : filter;
        return classRepository.findAllWithCategory().stream()
                .filter(c -> safe.categoryId() == null || c.getCategory().getId().equals(safe.categoryId()))
                .filter(c -> safe.trainerId() == null || c.getTrainerId().equals(safe.trainerId()))
                .filter(c -> safe.status() == null || c.getStatus().equals(safe.status()))
                .filter(c -> safe.titleContains() == null
                        || c.getTitle().toLowerCase(Locale.ROOT).contains(safe.titleContains().toLowerCase(Locale.ROOT)))
                .sorted(classComparator(safe.sortBy(), safe.sortDirection()))
                .map(this::toClassDto)
                .toList();
    }

    public ScheduleItemDto getScheduleById(UUID id) {
        return scheduleRepository.findDetailedById(id)
                .map(this::toScheduleDto)
                .orElseThrow(() -> new NotFoundException("Schedule", id));
    }

    public List<ScheduleItemDto> findSchedules(ScheduleFilter filter) {
        ScheduleFilter safe = filter == null
                ? new ScheduleFilter(null, null, null, null, null, null, null)
                : filter;
        List<ScheduleItemDto> items = scheduleRepository.findByFilter(
                        safe.trainerId(),
                        safe.categoryId(),
                        safe.status(),
                        toInstant(safe.startDate()),
                        toInstant(safe.endDate()))
                .stream()
                .map(this::toScheduleDto)
                .toList();
        return sortSchedules(items, safe.sortBy(), safe.sortDirection());
    }

    public List<ScheduleItemDto> findTrainerSchedule(UUID trainerId, OffsetDateTime from, OffsetDateTime to) {
        return scheduleRepository.findTrainerSchedule(trainerId, toInstant(from), toInstant(to)).stream()
                .map(this::toScheduleDto)
                .toList();
    }

    public ScheduleStatsDto scheduleStats(ScheduleFilter filter) {
        List<ScheduleItemDto> items = findSchedules(filter);
        int planned = (int) items.stream().filter(i -> "PLANNED".equals(i.status())).count();
        int completed = (int) items.stream().filter(i -> "COMPLETED".equals(i.status())).count();
        int cancelled = (int) items.stream().filter(i -> "CANCELLED".equals(i.status())).count();
        int slots = items.stream().mapToInt(ScheduleItemDto::availableSlots).sum();
        return new ScheduleStatsDto(items.size(), planned, completed, cancelled, slots);
    }

    private List<ScheduleItemDto> sortSchedules(List<ScheduleItemDto> items, String sortBy, String sortDirection) {
        Comparator<ScheduleItemDto> comparator = switch (sortBy == null ? "START_TIME" : sortBy) {
            case "AVAILABLE_SLOTS" -> Comparator.comparing(ScheduleItemDto::availableSlots);
            case "STATUS" -> Comparator.comparing(ScheduleItemDto::status);
            default -> Comparator.comparing(ScheduleItemDto::startTime);
        };
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return items.stream().sorted(comparator).toList();
    }

    private Comparator<ClassEntity> classComparator(String sortBy, String sortDirection) {
        Comparator<ClassEntity> comparator = switch (sortBy == null ? "TITLE" : sortBy.toUpperCase(Locale.ROOT)) {
            case "CAPACITY" -> Comparator.comparing(ClassEntity::getCapacity);
            case "STATUS" -> Comparator.comparing(ClassEntity::getStatus);
            default -> Comparator.comparing(ClassEntity::getTitle);
        };
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    ClassItemDto toClassDto(ClassEntity entity) {
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
