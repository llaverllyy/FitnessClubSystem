package com.example.fitnessclubsystem.training.persistence;

import com.example.fitnessclubsystem.training.domain.ScheduleEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {

    @EntityGraph(attributePaths = {"classEntity", "classEntity.category", "hall"})
    @Query("select s from ScheduleEntity s where s.id = :id")
    Optional<ScheduleEntity> findDetailedById(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"classEntity", "classEntity.category", "hall"})
    @Query("""
            select s from ScheduleEntity s
            where (:trainerId is null or s.trainerId = :trainerId)
              and (:categoryId is null or s.classEntity.category.id = :categoryId)
              and (:status is null or s.status = :status)
              and (:startDate is null or s.startTime >= :startDate)
              and (:endDate is null or s.startTime <= :endDate)
            order by s.startTime
            """)
    List<ScheduleEntity> findByFilter(
            @Param("trainerId") UUID trainerId,
            @Param("categoryId") UUID categoryId,
            @Param("status") String status,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @EntityGraph(attributePaths = {"classEntity", "classEntity.category", "hall"})
    @Query("""
            select s from ScheduleEntity s
            where s.trainerId = :trainerId
              and (:from is null or s.startTime >= :from)
              and (:to is null or s.startTime <= :to)
            order by s.startTime
            """)
    List<ScheduleEntity> findTrainerSchedule(
            @Param("trainerId") UUID trainerId,
            @Param("from") Instant from,
            @Param("to") Instant to);
}