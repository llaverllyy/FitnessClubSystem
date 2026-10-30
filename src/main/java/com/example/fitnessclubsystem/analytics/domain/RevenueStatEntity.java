package com.example.fitnessclubsystem.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "revenue_stats", schema = "analytics")
public class RevenueStatEntity {

    @Id
    private UUID id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "payments_count", nullable = false)
    private Integer paymentsCount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public Integer getPaymentsCount() {
        return paymentsCount;
    }

    public void setPaymentsCount(Integer paymentsCount) {
        this.paymentsCount = paymentsCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}