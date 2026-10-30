package com.example.fitnessclubsystem.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "class_popularity_stats", schema = "analytics")
public class ClassPopularityStatEntity {

    @Id
    private UUID id;

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "bookings_count", nullable = false)
    private Integer bookingsCount;

    @Column(name = "cancellations_count", nullable = false)
    private Integer cancellationsCount;

    @Column(name = "attendance_count", nullable = false)
    private Integer attendanceCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClassId() {
        return classId;
    }

    public void setClassId(UUID classId) {
        this.classId = classId;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(Integer bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public Integer getCancellationsCount() {
        return cancellationsCount;
    }

    public void setCancellationsCount(Integer cancellationsCount) {
        this.cancellationsCount = cancellationsCount;
    }

    public Integer getAttendanceCount() {
        return attendanceCount;
    }

    public void setAttendanceCount(Integer attendanceCount) {
        this.attendanceCount = attendanceCount;
    }
}