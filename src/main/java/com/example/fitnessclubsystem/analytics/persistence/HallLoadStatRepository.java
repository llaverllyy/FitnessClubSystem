package com.example.fitnessclubsystem.analytics.persistence;

import com.example.fitnessclubsystem.analytics.domain.HallLoadStatEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallLoadStatRepository extends JpaRepository<HallLoadStatEntity, UUID> {

    List<HallLoadStatEntity> findByStatDateBetween(LocalDate from, LocalDate to);
}