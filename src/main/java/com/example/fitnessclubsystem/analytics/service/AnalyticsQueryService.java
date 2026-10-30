package com.example.fitnessclubsystem.analytics.service;

import com.example.fitnessclubsystem.analytics.domain.ClassPopularityStatEntity;
import com.example.fitnessclubsystem.analytics.domain.ClientActivityStatEntity;
import com.example.fitnessclubsystem.analytics.domain.HallLoadStatEntity;
import com.example.fitnessclubsystem.analytics.domain.RevenueStatEntity;
import com.example.fitnessclubsystem.analytics.dto.ActiveClientItemDto;
import com.example.fitnessclubsystem.analytics.dto.AnalyticsReportDto;
import com.example.fitnessclubsystem.analytics.dto.HallLoadItemDto;
import com.example.fitnessclubsystem.analytics.dto.PopularClassItemDto;
import com.example.fitnessclubsystem.analytics.persistence.ClassPopularityStatRepository;
import com.example.fitnessclubsystem.analytics.persistence.ClientActivityStatRepository;
import com.example.fitnessclubsystem.analytics.persistence.HallLoadStatRepository;
import com.example.fitnessclubsystem.analytics.persistence.RevenueStatRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsQueryService {

    private final HallLoadStatRepository hallLoadStatRepository;
    private final ClassPopularityStatRepository classPopularityStatRepository;
    private final ClientActivityStatRepository clientActivityStatRepository;
    private final RevenueStatRepository revenueStatRepository;

    public AnalyticsQueryService(
            HallLoadStatRepository hallLoadStatRepository,
            ClassPopularityStatRepository classPopularityStatRepository,
            ClientActivityStatRepository clientActivityStatRepository,
            RevenueStatRepository revenueStatRepository) {
        this.hallLoadStatRepository = hallLoadStatRepository;
        this.classPopularityStatRepository = classPopularityStatRepository;
        this.clientActivityStatRepository = clientActivityStatRepository;
        this.revenueStatRepository = revenueStatRepository;
    }

    public AnalyticsReportDto getReport(LocalDate from, LocalDate to) {
        List<HallLoadStatEntity> hallLoad = hallLoadStatRepository.findByStatDateBetween(from, to);
        List<ClassPopularityStatEntity> popular = classPopularityStatRepository.findByStatDateBetween(from, to);
        List<ClientActivityStatEntity> clients = clientActivityStatRepository.findByStatMonthBetween(
                from.withDayOfMonth(1), to.withDayOfMonth(1));
        List<RevenueStatEntity> revenue = revenueStatRepository.findByStatDateBetween(from, to);

        double totalRevenue = revenue.stream()
                .map(RevenueStatEntity::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        int totalBookings = popular.stream().mapToInt(ClassPopularityStatEntity::getBookingsCount).sum();
        int totalAttendance = popular.stream().mapToInt(ClassPopularityStatEntity::getAttendanceCount).sum();

        return new AnalyticsReportDto(
                totalRevenue,
                totalBookings,
                totalAttendance,
                hallLoad.stream()
                        .map(h -> new HallLoadItemDto(
                                h.getHallId(), h.getStatDate().toString(), h.getTotalSlots(),
                                h.getBookedSlots(), h.getAttendedCount()))
                        .toList(),
                popular.stream()
                        .map(p -> new PopularClassItemDto(
                                p.getClassId(), p.getBookingsCount(),
                                p.getCancellationsCount(), p.getAttendanceCount()))
                        .toList(),
                clients.stream()
                        .map(c -> new ActiveClientItemDto(
                                c.getClientId(), c.getBookingsCount(),
                                c.getAttendanceCount(), c.getCancellationsCount()))
                        .toList()
        );
    }
}