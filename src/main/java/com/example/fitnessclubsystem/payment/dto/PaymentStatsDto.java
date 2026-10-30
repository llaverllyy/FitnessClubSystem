package com.example.fitnessclubsystem.payment.dto;

public record PaymentStatsDto(
        int totalCount,
        int completedCount,
        int failedCount,
        int pendingCount,
        double totalAmount,
        double completedAmount
) {
}
