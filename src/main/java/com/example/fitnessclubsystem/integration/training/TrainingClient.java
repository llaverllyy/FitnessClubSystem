package com.example.fitnessclubsystem.integration.training;

import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import java.util.Optional;
import java.util.UUID;

public interface TrainingClient {

    Optional<ScheduleItemDto> findScheduleById(UUID scheduleId);

    ScheduleItemDto requireAvailableSchedule(UUID scheduleId);

    ScheduleItemDto reserveSlot(UUID scheduleId);

    ScheduleItemDto releaseSlot(UUID scheduleId);
}
