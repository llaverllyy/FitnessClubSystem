package com.example.fitnessclubsystem.analytics.persistence;

import com.example.fitnessclubsystem.analytics.domain.ClientActivityStatEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientActivityStatRepository extends JpaRepository<ClientActivityStatEntity, UUID> {

    List<ClientActivityStatEntity> findByStatMonthBetween(LocalDate from, LocalDate to);
}