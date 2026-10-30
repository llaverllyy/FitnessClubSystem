package com.example.fitnessclubsystem.integration.booking;

import java.util.UUID;

public interface BookingClient {

    int cancelBySchedule(UUID scheduleId);
}
