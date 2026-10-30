package com.example.fitnessclubsystem.training.persistence;

import com.example.fitnessclubsystem.training.domain.ClassEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {

    @EntityGraph(attributePaths = "category")
    @Query("select c from ClassEntity c where c.id = :id")
    Optional<ClassEntity> findWithCategoryById(@Param("id") UUID id);

    @EntityGraph(attributePaths = "category")
    @Query("select c from ClassEntity c")
    List<ClassEntity> findAllWithCategory();
}