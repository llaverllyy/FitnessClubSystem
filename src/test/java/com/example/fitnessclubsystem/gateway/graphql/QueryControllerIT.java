package com.example.fitnessclubsystem.gateway.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.fitnessclubsystem.shared.DemoIds;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QueryControllerIT {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void meReturnsDemoClient() {
        Map<?, ?> body = execute("""
                query {
                  me {
                    id
                    email
                    role
                    client { id fullName }
                  }
                }
                """);

        assertThat(body.get("errors")).isNull();
        Map<?, ?> me = asMap(asMap(body.get("data")).get("me"));
        assertThat(me.get("email")).isEqualTo("client@fitness.local");
        assertThat(me.get("role")).isEqualTo("CLIENT");
        assertThat(asMap(me.get("client")).get("id")).isEqualTo(DemoIds.CLIENT.toString());
    }

    @Test
    void scheduleReturnsItemsWithRelatedClass() {
        Map<?, ?> body = execute("""
                query {
                  schedule {
                    id
                    status
                    hallName
                    classItem { title }
                  }
                }
                """);

        assertThat(body.get("errors")).isNull();
        List<?> schedule = (List<?>) asMap(body.get("data")).get("schedule");
        assertThat(schedule).isNotEmpty();
        assertThat(asMap(asMap(schedule.getFirst()).get("classItem")).get("title"))
                .isEqualTo("Morning Yoga");
    }

    @Test
    void classByIdWorks() {
        Map<?, ?> body = execute("""
                query {
                  classById(id: "%s") { id title trainerId }
                }
                """.formatted(DemoIds.CLASS_YOGA));

        assertThat(body.get("errors")).isNull();
        assertThat(asMap(asMap(body.get("data")).get("classById")).get("title"))
                .isEqualTo("Morning Yoga");
    }

    @Test
    void myBookingsEnrichesScheduleData() {
        Map<?, ?> body = execute("""
                query {
                  myBookings {
                    id
                    status
                    classTitle
                    scheduleStartTime
                  }
                }
                """);

        assertThat(body.get("errors")).isNull();
        Map<?, ?> booking = asMap(((List<?>) asMap(body.get("data")).get("myBookings")).getFirst());
        assertThat(booking.get("status")).isEqualTo("BOOKED");
        assertThat(booking.get("classTitle")).isEqualTo("Morning Yoga");
        assertThat(booking.get("scheduleStartTime")).isNotNull();
    }

    @Test
    void mySubscriptionsAndPayments() {
        Map<?, ?> body = execute("""
                query {
                  mySubscriptions { id status planName remainingVisits }
                  myPayments { id status amount currency }
                }
                """);

        assertThat(body.get("errors")).isNull();
        Map<?, ?> data = asMap(body.get("data"));
        assertThat(asMap(((List<?>) data.get("mySubscriptions")).getFirst()).get("status"))
                .isEqualTo("ACTIVE");
        assertThat(asMap(((List<?>) data.get("myPayments")).getFirst()).get("status"))
                .isEqualTo("COMPLETED");
    }

    @Test
    void myNotificationsAndAnalytics() {
        Map<?, ?> body = execute("""
                query {
                  myNotifications { id title status }
                  analytics(filter: { fromDate: "2020-01-01", toDate: "2030-12-31" }) {
                    totalRevenue
                    totalBookings
                    hallLoad { hallId }
                  }
                }
                """);

        assertThat(body.get("errors")).isNull();
        Map<?, ?> data = asMap(body.get("data"));
        assertThat(asMap(((List<?>) data.get("myNotifications")).getFirst()).get("title"))
                .isEqualTo("Запись подтверждена");
        assertThat(asMap(data.get("analytics")).get("totalRevenue")).isEqualTo(4500.0);
    }

    @Test
    void missingEntityReturnsNotFound() {
        Map<?, ?> body = execute("""
                query {
                  bookingById(id: "00000000-0000-0000-0000-000000000099") { id }
                }
                """);

        List<?> errors = (List<?>) body.get("errors");
        assertThat(errors).isNotEmpty();
        assertThat(asMap(errors.getFirst()).get("message").toString()).contains("Booking not found");
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
