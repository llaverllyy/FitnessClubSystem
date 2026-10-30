package com.example.fitnessclubsystem.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "hall_load_stats", schema = "analytics")
public class HallLoadStatEntity {

    @Id
    private UUID id;

    @Column(name = "hall_id", nullable = false)
    private UUID hallId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "booked_slots", nullable = false)
    private Integer bookedSlots;

    @Column(name = "attended_count", nullable = false)
    private Integer attendedCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHallId() {
        return hallId;
    }

    public void setHallId(UUID hallId) {
        this.hallId = hallId;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(Integer totalSlots) {
        this.totalSlots = totalSlots;
    }

    public Integer getBookedSlots() {
        return bookedSlots;
    }

    public void setBookedSlots(Integer bookedSlots) {
        this.bookedSlots = bookedSlots;
    }

    public Integer getAttendedCount() {
        return attendedCount;
    }

    public void setAttendedCount(Integer attendedCount) {
        this.attendedCount = attendedCount;
    }
}