package com.example.fitnessclubsystem.analytics.persistence;

import com.example.fitnessclubsystem.analytics.domain.ClassPopularityStatEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassPopularityStatRepository extends JpaRepository<ClassPopularityStatEntity, UUID> {

    List<ClassPopularityStatEntity> findByStatDateBetween(LocalDate from, LocalDate to);
}