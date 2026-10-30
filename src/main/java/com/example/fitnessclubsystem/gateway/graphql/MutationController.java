package com.example.fitnessclubsystem.gateway.graphql;

import com.example.fitnessclubsystem.booking.dto.BookingItemDto;
import com.example.fitnessclubsystem.booking.service.BookingCommandService;
import com.example.fitnessclubsystem.identity.dto.AuthPayloadDto;
import com.example.fitnessclubsystem.identity.service.IdentityCommandService;
import com.example.fitnessclubsystem.payment.dto.PaymentItemDto;
import com.example.fitnessclubsystem.payment.dto.SubscriptionPurchaseDto;
import com.example.fitnessclubsystem.payment.service.PaymentCommandService;
import com.example.fitnessclubsystem.training.dto.ClassItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.service.TrainingCommandService;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MutationController {

    private final IdentityCommandService identityCommandService;
    private final TrainingCommandService trainingCommandService;
    private final PaymentCommandService paymentCommandService;
    private final BookingCommandService bookingCommandService;

    public MutationController(
            IdentityCommandService identityCommandService,
            TrainingCommandService trainingCommandService,
            PaymentCommandService paymentCommandService,
            BookingCommandService bookingCommandService) {
        this.identityCommandService = identityCommandService;
        this.trainingCommandService = trainingCommandService;
        this.paymentCommandService = paymentCommandService;
        this.bookingCommandService = bookingCommandService;
    }

    @MutationMapping
    public AuthPayloadDto registerUser(@Argument Map<String, Object> input) {
        return identityCommandService.register(
                (String) input.get("email"),
                (String) input.get("password"),
                (String) input.get("role"),
                (String) input.get("fullName"),
                (String) input.get("phone"),
                (String) input.get("birthDate"),
                (String) input.get("specialization"));
    }

    @MutationMapping
    public AuthPayloadDto login(@Argument Map<String, Object> input) {
        return identityCommandService.login(
                (String) input.get("email"),
                (String) input.get("password"));
    }

    @MutationMapping
    public ClassItemDto createClass(@Argument Map<String, Object> input) {
        return trainingCommandService.createClass(
                (String) input.get("title"),
                (String) input.get("description"),
                ((Number) input.get("durationMinutes")).intValue(),
                ((Number) input.get("capacity")).intValue(),
                UUID.fromString((String) input.get("categoryId")),
                UUID.fromString((String) input.get("trainerId")));
    }

    @MutationMapping
    public ClassItemDto updateClass(@Argument Map<String, Object> input) {
        return trainingCommandService.updateClass(
                UUID.fromString((String) input.get("id")),
                (String) input.get("title"),
                (String) input.get("description"),
                input.get("durationMinutes") == null ? null : ((Number) input.get("durationMinutes")).intValue(),
                input.get("capacity") == null ? null : ((Number) input.get("capacity")).intValue(),
                input.get("categoryId") == null ? null : UUID.fromString((String) input.get("categoryId")),
                input.get("trainerId") == null ? null : UUID.fromString((String) input.get("trainerId")),
                (String) input.get("status"));
    }

    @MutationMapping
    public ScheduleItemDto createSchedule(@Argument Map<String, Object> input) {
        return trainingCommandService.createSchedule(
                UUID.fromString((String) input.get("classId")),
                UUID.fromString((String) input.get("hallId")),
                UUID.fromString((String) input.get("trainerId")),
                OffsetDateTime.parse(input.get("startTime").toString()),
                OffsetDateTime.parse(input.get("endTime").toString()));
    }

    @MutationMapping
    public ScheduleItemDto changeScheduleStatus(@Argument Map<String, Object> input) {
        return trainingCommandService.changeStatus(
                UUID.fromString((String) input.get("scheduleId")),
                (String) input.get("status"));
    }

    @MutationMapping
    public SubscriptionPurchaseDto purchaseSubscription(@Argument Map<String, Object> input) {
        return paymentCommandService.purchaseSubscription(
                UUID.fromString((String) input.get("clientId")),
                UUID.fromString((String) input.get("planId")),
                (String) input.get("paymentMethod"));
    }

    @MutationMapping
    public PaymentItemDto confirmPayment(@Argument Map<String, Object> input) {
        return paymentCommandService.confirmPayment(
                UUID.fromString((String) input.get("paymentId")),
                (String) input.get("status"));
    }

    @MutationMapping
    public BookingItemDto bookSchedule(@Argument Map<String, Object> input) {
        return bookingCommandService.book(
                UUID.fromString((String) input.get("clientId")),
                UUID.fromString((String) input.get("scheduleId")),
                UUID.fromString((String) input.get("subscriptionId")));
    }

    @MutationMapping
    public BookingItemDto cancelBooking(@Argument Map<String, Object> input) {
        return bookingCommandService.cancel(UUID.fromString((String) input.get("bookingId")));
    }

    @MutationMapping
    public BookingItemDto markAttendance(@Argument Map<String, Object> input) {
        return bookingCommandService.markAttendance(
                UUID.fromString((String) input.get("bookingId")),
                (String) input.get("status"));
    }
}
