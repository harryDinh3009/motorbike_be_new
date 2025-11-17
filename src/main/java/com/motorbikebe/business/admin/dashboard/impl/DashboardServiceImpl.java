package com.motorbikebe.business.admin.dashboard.impl;

import com.motorbikebe.business.admin.dashboard.service.DashboardService;
import com.motorbikebe.dto.business.admin.dashboard.*;
import com.motorbikebe.repository.business.admin.CarRepository;
import com.motorbikebe.repository.business.admin.ContractRepository;
import com.motorbikebe.repository.projection.ContractRevenueProjection;
import com.motorbikebe.repository.projection.DailyRevenueProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ContractRepository contractRepository;
    private final CarRepository carRepository;

    @Override
    public DashboardResponseDTO getDashboard(String branchId) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = currentMonthStart.plusMonths(1);
        LocalDate lastMonthStart = currentMonthStart.minusMonths(1);

        DashboardRevenueBlockDTO todayRevenue = toRevenueBlock(
                contractRepository.sumRevenueByBranchAndDate(branchId, toDate(today), toDate(tomorrow)));

        DashboardRevenueBlockDTO thisMonthRevenue = toRevenueBlock(
                contractRepository.sumRevenueByBranchAndDate(branchId, toDate(currentMonthStart), toDate(nextMonthStart)));

        DashboardRevenueBlockDTO lastMonthRevenue = toRevenueBlock(
                contractRepository.sumRevenueByBranchAndDate(branchId, toDate(lastMonthStart), toDate(currentMonthStart)));

        long totalContracts = contractRepository.countContractsByBranchAndDate(
                branchId, toDate(currentMonthStart), toDate(nextMonthStart));

        long totalCars = carRepository.countActiveCarsByBranch(branchId);

        DashboardPerformanceDTO performance = DashboardPerformanceDTO.builder()
                .totalContracts(totalContracts)
                .totalCars(totalCars)
                .totalRevenue(thisMonthRevenue.getTotalAmount())
                .build();

        DashboardRevenueOverviewDTO overview = DashboardRevenueOverviewDTO.builder()
                .today(todayRevenue)
                .thisMonth(thisMonthRevenue)
                .lastMonth(lastMonthRevenue)
                .build();

        List<DashboardDailyRevenueDTO> daily = contractRepository
                .sumDailyRevenueByBranchAndDate(branchId, toDate(currentMonthStart), toDate(nextMonthStart))
                .stream()
                .map(this::toDailyRevenueDTO)
                .collect(Collectors.toList());

        return DashboardResponseDTO.builder()
                .performance(performance)
                .revenueOverview(overview)
                .dailyRevenue(daily)
                .build();
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private DashboardRevenueBlockDTO toRevenueBlock(ContractRevenueProjection projection) {
        if (projection == null) {
            return DashboardRevenueBlockDTO.builder()
                    .contractAmount(BigDecimal.ZERO)
                    .rentalAmount(BigDecimal.ZERO)
                    .surchargeAmount(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal contractAmount = defaultBigDecimal(projection.getContractAmount());
        BigDecimal rentalAmount = defaultBigDecimal(projection.getRentalAmount());
        BigDecimal surchargeAmount = defaultBigDecimal(projection.getSurchargeAmount());

        return DashboardRevenueBlockDTO.builder()
                .contractAmount(contractAmount)
                .rentalAmount(rentalAmount)
                .surchargeAmount(surchargeAmount)
                .totalAmount(contractAmount)
                .build();
    }

    private DashboardDailyRevenueDTO toDailyRevenueDTO(DailyRevenueProjection projection) {
        if (projection == null) {
            return DashboardDailyRevenueDTO.builder()
                    .date(null)
                    .contractAmount(BigDecimal.ZERO)
                    .rentalAmount(BigDecimal.ZERO)
                    .surchargeAmount(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal contractAmount = defaultBigDecimal(projection.getContractAmount());
        BigDecimal rentalAmount = defaultBigDecimal(projection.getRentalAmount());
        BigDecimal surchargeAmount = defaultBigDecimal(projection.getSurchargeAmount());

        return DashboardDailyRevenueDTO.builder()
                .date(projection.getRevenueDate())
                .contractAmount(contractAmount)
                .rentalAmount(rentalAmount)
                .surchargeAmount(surchargeAmount)
                .totalAmount(contractAmount)
                .build();
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

