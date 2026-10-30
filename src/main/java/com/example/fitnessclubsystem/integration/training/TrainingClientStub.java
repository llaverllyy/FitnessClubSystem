package com.example.fitnessclubsystem.integration.training;

import com.example.fitnessclubsystem.shared.exception.NotFoundException;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.service.TrainingCommandService;
import com.example.fitnessclubsystem.training.service.TrainingQueryService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TrainingClientStub implements TrainingClient {

    private final TrainingQueryService trainingQueryService;
    private final TrainingCommandService trainingCommandService;

    public TrainingClientStub(
            TrainingQueryService trainingQueryService,
            TrainingCommandService trainingCommandService) {
        this.trainingQueryService = trainingQueryService;
        this.trainingCommandService = trainingCommandService;
    }

    @Override
    public Optional<ScheduleItemDto> findScheduleById(UUID scheduleId) {
        try {
            return Optional.of(trainingQueryService.getScheduleById(scheduleId));
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public ScheduleItemDto requireAvailableSchedule(UUID scheduleId) {
        return trainingCommandService.requireAvailableForBooking(scheduleId);
    }

    @Override
    public ScheduleItemDto reserveSlot(UUID scheduleId) {
        return trainingCommandService.reserveSlot(scheduleId);
    }

    @Override
    public ScheduleItemDto releaseSlot(UUID scheduleId) {
        return trainingCommandService.releaseSlot(scheduleId);
    }
}
