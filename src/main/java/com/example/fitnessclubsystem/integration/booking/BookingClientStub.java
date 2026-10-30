package com.example.fitnessclubsystem.integration.booking;

import com.example.fitnessclubsystem.booking.service.BookingCommandService;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BookingClientStub implements BookingClient {

    private final BookingCommandService bookingCommandService;

    public BookingClientStub(BookingCommandService bookingCommandService) {
        this.bookingCommandService = bookingCommandService;
    }

    @Override
    public int cancelBySchedule(UUID scheduleId) {
        return bookingCommandService.cancelByClubForSchedule(scheduleId);
    }
}
