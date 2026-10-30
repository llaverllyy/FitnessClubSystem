package com.example.fitnessclubsystem.analytics.dto;

import java.util.List;

public record AnalyticsReportDto(
        Double totalRevenue,
        Integer totalBookings,
        Integer totalAttendance,
        List<HallLoadItemDto> hallLoad,
        List<PopularClassItemDto> popularClasses,
        List<ActiveClientItemDto> activeClients
) {
}