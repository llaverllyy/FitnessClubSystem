package com.example.fitnessclubsystem.analytics.persistence;

import com.example.fitnessclubsystem.analytics.domain.RevenueStatEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueStatRepository extends JpaRepository<RevenueStatEntity, UUID> {

    List<RevenueStatEntity> findByStatDateBetween(LocalDate from, LocalDate to);
}