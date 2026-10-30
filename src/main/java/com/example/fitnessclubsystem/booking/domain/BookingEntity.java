package com.example.fitnessclubsystem.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings", schema = "booking")
public class BookingEntity {

    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "booked_at", nullable = false)
    private Instant bookedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "attendance_marked_at")
    private Instant attendanceMarkedAt;

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

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Instant bookedAt) {
        this.bookedAt = bookedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getAttendanceMarkedAt() {
        return attendanceMarkedAt;
    }

    public void setAttendanceMarkedAt(Instant attendanceMarkedAt) {
        this.attendanceMarkedAt = attendanceMarkedAt;
    }
}