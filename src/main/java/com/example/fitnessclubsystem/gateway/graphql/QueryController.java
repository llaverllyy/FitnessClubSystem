package com.example.fitnessclubsystem.gateway.graphql;

import com.example.fitnessclubsystem.analytics.dto.AnalyticsReportDto;
import com.example.fitnessclubsystem.analytics.service.AnalyticsQueryService;
import com.example.fitnessclubsystem.booking.dto.BookingFilter;
import com.example.fitnessclubsystem.booking.dto.BookingItemDto;
import com.example.fitnessclubsystem.booking.dto.BookingStatsDto;
import com.example.fitnessclubsystem.booking.service.BookingQueryService;
import com.example.fitnessclubsystem.identity.dto.UserProfileDto;
import com.example.fitnessclubsystem.identity.service.IdentityQueryService;
import com.example.fitnessclubsystem.membership.dto.SubscriptionItemDto;
import com.example.fitnessclubsystem.membership.dto.SubscriptionPlanItemDto;
import com.example.fitnessclubsystem.membership.service.MembershipQueryService;
import com.example.fitnessclubsystem.notification.dto.NotificationItemDto;
import com.example.fitnessclubsystem.notification.service.NotificationQueryService;
import com.example.fitnessclubsystem.payment.dto.PaymentItemDto;
import com.example.fitnessclubsystem.payment.dto.PaymentStatsDto;
import com.example.fitnessclubsystem.payment.dto.PaymentStatsFilter;
import com.example.fitnessclubsystem.payment.service.PaymentQueryService;
import com.example.fitnessclubsystem.shared.context.CurrentUserProvider;
import com.example.fitnessclubsystem.training.dto.ClassFilter;
import com.example.fitnessclubsystem.training.dto.ClassItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleFilter;
import com.example.fitnessclubsystem.training.dto.ScheduleItemDto;
import com.example.fitnessclubsystem.training.dto.ScheduleStatsDto;
import com.example.fitnessclubsystem.training.service.TrainingQueryService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class QueryController {

    private final CurrentUserProvider currentUserProvider;
    private final IdentityQueryService identityQueryService;
    private final TrainingQueryService trainingQueryService;
    private final BookingQueryService bookingQueryService;
    private final MembershipQueryService membershipQueryService;
    private final PaymentQueryService paymentQueryService;
    private final NotificationQueryService notificationQueryService;
    private final AnalyticsQueryService analyticsQueryService;

    public QueryController(
            CurrentUserProvider currentUserProvider,
            IdentityQueryService identityQueryService,
            TrainingQueryService trainingQueryService,
            BookingQueryService bookingQueryService,
            MembershipQueryService membershipQueryService,
            PaymentQueryService paymentQueryService,
            NotificationQueryService notificationQueryService,
            AnalyticsQueryService analyticsQueryService) {
        this.currentUserProvider = currentUserProvider;
        this.identityQueryService = identityQueryService;
        this.trainingQueryService = trainingQueryService;
        this.bookingQueryService = bookingQueryService;
        this.membershipQueryService = membershipQueryService;
        this.paymentQueryService = paymentQueryService;
        this.notificationQueryService = notificationQueryService;
        this.analyticsQueryService = analyticsQueryService;
    }

    @QueryMapping
    public UserProfileDto me() {
        return identityQueryService.getUserProfile(currentUserProvider.userId());
    }

    @QueryMapping
    public UserProfileDto user(@Argument UUID id) {
        return identityQueryService.getUserProfile(id);
    }

    @QueryMapping
    public ClassItemDto classById(@Argument UUID id) {
        return trainingQueryService.getClassById(id);
    }

    @QueryMapping
    public List<ClassItemDto> classes(
            @Argument Map<String, Object> filter,
            @Argument String sortBy,
            @Argument String sortDirection) {
        return trainingQueryService.findClasses(toClassFilter(filter, sortBy, sortDirection));
    }

    @QueryMapping
    public List<ScheduleItemDto> schedule(
            @Argument ScheduleFilterInput filter,
            @Argument String sortBy,
            @Argument String sortDirection) {
        ScheduleFilter serviceFilter = filter == null
                ? new ScheduleFilter(null, null, null, null, null, sortBy, sortDirection)
                : new ScheduleFilter(
                        filter.trainerId(),
                        filter.categoryId(),
                        filter.startDate(),
                        filter.endDate(),
                        filter.status(),
                        sortBy,
                        sortDirection);
        return trainingQueryService.findSchedules(serviceFilter);
    }

    @QueryMapping
    public ScheduleItemDto scheduleById(@Argument UUID id) {
        return trainingQueryService.getScheduleById(id);
    }

    @QueryMapping
    public List<ScheduleItemDto> trainerSchedule(
            @Argument UUID trainerId,
            @Argument OffsetDateTime from,
            @Argument OffsetDateTime to) {
        return trainingQueryService.findTrainerSchedule(trainerId, from, to);
    }

    @QueryMapping
    public List<BookingItemDto> myBookings(
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortDirection) {
        return bookingQueryService.findByClient(
                currentUserProvider.clientId(), status, sortBy, sortDirection);
    }

    @QueryMapping
    public List<BookingItemDto> bookings(
            @Argument Map<String, Object> filter,
            @Argument String sortBy,
            @Argument String sortDirection) {
        return bookingQueryService.find(toBookingFilter(filter, sortBy, sortDirection));
    }

    @QueryMapping
    public BookingItemDto bookingById(@Argument UUID id) {
        return bookingQueryService.getById(id);
    }

    @QueryMapping
    public List<SubscriptionItemDto> mySubscriptions() {
        return membershipQueryService.findByClientId(currentUserProvider.clientId());
    }

    @QueryMapping
    public SubscriptionItemDto subscriptionById(@Argument UUID id) {
        return membershipQueryService.getById(id);
    }

    @QueryMapping
    public List<SubscriptionPlanItemDto> subscriptionPlans(@Argument Boolean activeOnly) {
        return membershipQueryService.findPlans(activeOnly == null || activeOnly);
    }

    @QueryMapping
    public List<PaymentItemDto> myPayments() {
        return paymentQueryService.findByClientId(currentUserProvider.clientId());
    }

    @QueryMapping
    public PaymentItemDto paymentById(@Argument UUID id) {
        return paymentQueryService.getById(id);
    }

    @QueryMapping
    public List<NotificationItemDto> myNotifications(@Argument Boolean unreadOnly) {
        return notificationQueryService.findByUser(
                currentUserProvider.userId(),
                Boolean.TRUE.equals(unreadOnly));
    }

    @QueryMapping
    public AnalyticsReportDto analytics(@Argument AnalyticsFilterInput filter) {
        return analyticsQueryService.getReport(
                LocalDate.parse(filter.fromDate()),
                LocalDate.parse(filter.toDate()));
    }

    @QueryMapping
    public BookingStatsDto bookingStats(@Argument Map<String, Object> filter) {
        return bookingQueryService.stats(toBookingFilter(filter, null, null));
    }

    @QueryMapping
    public PaymentStatsDto paymentStats(@Argument Map<String, Object> filter) {
        UUID clientId = filter == null || filter.get("clientId") == null
                ? null
                : UUID.fromString(filter.get("clientId").toString());
        String status = filter == null ? null : (String) filter.get("status");
        return paymentQueryService.stats(new PaymentStatsFilter(clientId, status));
    }

    @QueryMapping
    public ScheduleStatsDto scheduleStats(@Argument ScheduleFilterInput filter) {
        ScheduleFilter serviceFilter = filter == null
                ? null
                : new ScheduleFilter(
                        filter.trainerId(),
                        filter.categoryId(),
                        filter.startDate(),
                        filter.endDate(),
                        filter.status());
        return trainingQueryService.scheduleStats(serviceFilter);
    }

    private ClassFilter toClassFilter(Map<String, Object> filter, String sortBy, String sortDirection) {
        if (filter == null) {
            return new ClassFilter(null, null, null, null, sortBy, sortDirection);
        }
        return new ClassFilter(
                filter.get("categoryId") == null ? null : UUID.fromString(filter.get("categoryId").toString()),
                filter.get("trainerId") == null ? null : UUID.fromString(filter.get("trainerId").toString()),
                (String) filter.get("status"),
                (String) filter.get("titleContains"),
                sortBy,
                sortDirection);
    }

    private BookingFilter toBookingFilter(Map<String, Object> filter, String sortBy, String sortDirection) {
        if (filter == null) {
            return new BookingFilter(null, null, null, sortBy, sortDirection);
        }
        return new BookingFilter(
                filter.get("clientId") == null ? null : UUID.fromString(filter.get("clientId").toString()),
                filter.get("scheduleId") == null ? null : UUID.fromString(filter.get("scheduleId").toString()),
                (String) filter.get("status"),
                sortBy,
                sortDirection);
    }
}
