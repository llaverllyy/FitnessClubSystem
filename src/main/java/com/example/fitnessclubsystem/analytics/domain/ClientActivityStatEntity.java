package com.example.fitnessclubsystem.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "client_activity_stats", schema = "analytics")
public class ClientActivityStatEntity {

    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "stat_month", nullable = false)
    private LocalDate statMonth;

    @Column(name = "bookings_count", nullable = false)
    private Integer bookingsCount;

    @Column(name = "attendance_count", nullable = false)
    private Integer attendanceCount;

    @Column(name = "cancellations_count", nullable = false)
    private Integer cancellationsCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public LocalDate getStatMonth() {
        return statMonth;
    }

    public void setStatMonth(LocalDate statMonth) {
        this.statMonth = statMonth;
    }

    public Integer getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(Integer bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public Integer getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(Integer attendanceCount) {
        this.attendanceCount = attendanceCount;
    }

    public Integer getCancellationsCount() {
        return cancellationsCount;
    }

    public void setCancellationsCount(Integer cancellationsCount) {
        this.cancellationsCount = cancellationsCount;
    }
}