package com.example.fitnessclubsystem.integration.training;

import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.service.TrainingQueryService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TrainingClientStub implements TrainingClient {

    private final TrainingQueryService trainingQueryService;

    public TrainingClientStub(TrainingQueryService trainingQueryService) {
        this.trainingQueryService = trainingQueryService;
    }

    @Override
    public Optional<ScheduleItemDto> findScheduleById(UUID scheduleId) {
        try {
            return Optional.of(trainingQueryService.getScheduleById(scheduleId));
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
    }
}