package com.example.fitnessclubsystem.gateway.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fitnessclubsystem.shared.DemoIds;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MutationControllerIT {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void purchaseConfirmAndBookFlow() {
        Map<?, ?> purchase = execute("""
                mutation {
                  purchaseSubscription(input: {
                    clientId: "%s"
                    planId: "%s"
                    paymentMethod: "CARD"
                  }) {
                    subscription { id status }
                    payment { id status amount }
                  }
                }
                """.formatted(DemoIds.CLIENT, DemoIds.PLAN_STANDARD));
        assertThat(purchase.get("errors")).isNull();
        Map<?, ?> purchaseData = asMap(asMap(purchase.get("data")).get("purchaseSubscription"));
        String paymentId = asMap(purchaseData.get("payment")).get("id").toString();
        String subscriptionId = asMap(purchaseData.get("subscription")).get("id").toString();
        assertThat(asMap(purchaseData.get("subscription")).get("status")).isEqualTo("PENDING");

        Map<?, ?> confirm = execute("""
                mutation {
                  confirmPayment(input: { paymentId: "%s", status: COMPLETED }) {
                    id status
                  }
                }
                """.formatted(paymentId));
        assertThat(confirm.get("errors")).isNull();
        assertThat(asMap(asMap(confirm.get("data")).get("confirmPayment")).get("status"))
                .isEqualTo("COMPLETED");

        Map<?, ?> book = execute("""
                mutation {
                  bookSchedule(input: {
                    clientId: "%s"
                    scheduleId: "%s"
                    subscriptionId: "%s"
                  }) {
                    id status classTitle
                  }
                }
                """.formatted(DemoIds.CLIENT, DemoIds.SCHEDULE_2, subscriptionId));
        assertThat(book.get("errors")).isNull();
        assertThat(asMap(asMap(book.get("data")).get("bookSchedule")).get("status")).isEqualTo("BOOKED");
    }

    @Test
    void cancelBookingBusinessRule() {
        Map<?, ?> purchase = execute("""
                mutation {
                  purchaseSubscription(input: {
                    clientId: "%s"
                    planId: "%s"
                    paymentMethod: "CARD"
                  }) {
                    subscription { id }
                    payment { id }
                  }
                }
                """.formatted(DemoIds.CLIENT, DemoIds.PLAN_STANDARD));
        Map<?, ?> purchaseData = asMap(asMap(purchase.get("data")).get("purchaseSubscription"));
        String paymentId = asMap(purchaseData.get("payment")).get("id").toString();
        String subscriptionId = asMap(purchaseData.get("subscription")).get("id").toString();

        execute("""
                mutation {
                  confirmPayment(input: { paymentId: "%s", status: COMPLETED }) { id }
                }
                """.formatted(paymentId));

        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(10);
        Map<?, ?> schedule = execute("""
                mutation {
                  createSchedule(input: {
                    classId: "%s"
                    hallId: "%s"
                    trainerId: "%s"
                    startTime: "%s"
                    endTime: "%s"
                  }) { id }
                }
                """.formatted(DemoIds.CLASS_YOGA, DemoIds.HALL_A, DemoIds.TRAINER, start, start.plusHours(1)));
        String scheduleId = asMap(asMap(schedule.get("data")).get("createSchedule")).get("id").toString();

        Map<?, ?> book = execute("""
                mutation {
                  bookSchedule(input: {
                    clientId: "%s"
                    scheduleId: "%s"
                    subscriptionId: "%s"
                  }) { id }
                }
                """.formatted(DemoIds.CLIENT, scheduleId, subscriptionId));
        String bookingId = asMap(asMap(book.get("data")).get("bookSchedule")).get("id").toString();

        Map<?, ?> cancel = execute("""
                mutation {
                  cancelBooking(input: { bookingId: "%s" }) {
                    id status cancelledAt
                  }
                }
                """.formatted(bookingId));
        assertThat(cancel.get("errors")).isNull();
        assertThat(asMap(asMap(cancel.get("data")).get("cancelBooking")).get("status"))
                .isEqualTo("CANCELLED");
    }

    @Test
    void filterSortAndAggregations() {
        Map<?, ?> body = execute("""
                query {
                  schedule(sortBy: AVAILABLE_SLOTS, sortDirection: ASC) { id availableSlots }
                  bookingStats { totalCount bookedCount cancelledCount }
                  paymentStats { totalCount completedAmount }
                  scheduleStats { totalCount plannedCount totalAvailableSlots }
                  classes(filter: { titleContains: "Yoga" }, sortBy: "TITLE", sortDirection: ASC) {
                    title
                  }
                }
                """);
        assertThat(body.get("errors")).isNull();
        Map<?, ?> data = asMap(body.get("data"));
        assertThat((List<?>) data.get("schedule")).isNotEmpty();
        assertThat(asMap(data.get("bookingStats")).get("totalCount")).isNotNull();
        assertThat(asMap(data.get("paymentStats")).get("completedAmount")).isNotNull();
        assertThat(((List<?>) data.get("classes")).size()).isGreaterThan(0);
    }

    @Test
    void createScheduleRequiresValidInterval() {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(5);
        Map<?, ?> body = execute("""
                mutation {
                  createSchedule(input: {
                    classId: "%s"
                    hallId: "%s"
                    trainerId: "%s"
                    startTime: "%s"
                    endTime: "%s"
                  }) { id status availableSlots }
                }
                """.formatted(
                DemoIds.CLASS_YOGA,
                DemoIds.HALL_A,
                DemoIds.TRAINER,
                start,
                start.plusHours(1)));
        assertThat(body.get("errors")).isNull();
        assertThat(asMap(asMap(body.get("data")).get("createSchedule")).get("status"))
                .isEqualTo("PLANNED");
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> execute(String query) {
        return restClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("query", query))
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> asMap(Object value) {
        return (Map<?, ?>) value;
    }
}
