package com.expenseapp.app.service;


import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.ReportRepository;
import com.expenseapp.app.dto.report.models.CategoryDistribution;
import com.expenseapp.app.dto.report.models.MonthlyTrend;
import com.expenseapp.app.dto.report.models.Summary;
import com.expenseapp.app.dto.report.models.TopMerchant;
import com.expenseapp.app.dto.report.request.ReportQueryRequest;
import com.expenseapp.app.dto.report.response.ReportOverviewResponse;
import com.expenseapp.app.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final EmailAccountRepository emailAccountRepository;
    private final ReportRepository reportRepository;

    private static final int TOP_MERCHANTS_LIMIT = 5;
    private static final BigDecimal DEFAULT_BUDGET = new BigDecimal("6000"); // TODO ; implement budget entry

    // Month abbreviations map
    private static final Map<Integer, String> MONTH_LABELS = Map.ofEntries(
            Map.entry(1, "JAN"), Map.entry(2, "FEB"), Map.entry(3, "MAR"),
            Map.entry(4, "APR"), Map.entry(5, "MAY"), Map.entry(6, "JUN"),
            Map.entry(7, "JUL"), Map.entry(8, "AUG"), Map.entry(9, "SEP"),
            Map.entry(10, "OCT"), Map.entry(11, "NOV"), Map.entry(12, "DEC")
    );

    @Override
    public ReportOverviewResponse getOverview(User user, ReportQueryRequest request) {
        List<UUID> accountIds = resolveAccountIds(request, user);
        LocalDate start       = request.startDate();
        LocalDate end         = request.endDate();
        TransactionType type = TransactionType.DEBIT;

        return ReportOverviewResponse.builder()
                .summary(buildSummary(user, type, accountIds, start, end))
                .monthlyTrends(buildMonthlyTrends(user, type, accountIds, start, end))
                .categoryDistributions(buildCategoryDistributions(user, type, accountIds, start, end))
                .topMerchants(buildTopMerchants(user, type, accountIds, start, end))
                .build();
    }



    // --- Summary ---

    private Summary buildSummary(User user, TransactionType type, List<UUID> accountIds,
                                 LocalDate start,
                                 LocalDate end) {

        start = LocalDate.now().withDayOfMonth(1);
        end = LocalDate.now();
        BigDecimal thisMonth = reportRepository.sumAmountByAccounts(user, type, accountIds, start, end);

        // Previous period of same length for % change
        LocalDate prevMonthStart = start.minusMonths(1);
        LocalDate prevMonthEnd = end.minusMonths(1);

        BigDecimal prevMonth = reportRepository.sumAmountByAccounts(user, type, accountIds, prevMonthStart, prevMonthEnd);

        Double percentageChange = calculatePercentageChange(prevMonth, thisMonth);

        // Daily average implementation
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        long days         = daysBetween == 0 ? 1 : daysBetween + 1;
        BigDecimal daily  = thisMonth.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

        // Previous daily average for daily change %
        long prevDays          = ChronoUnit.DAYS.between(prevMonthStart, prevMonthEnd) + 1;
        BigDecimal prevDaily   = prevMonth.divide(BigDecimal.valueOf(prevDays), 2, RoundingMode.HALF_UP);
        Double dailyChange     = calculatePercentageChange(prevDaily, daily);

        double budgetUtil = DEFAULT_BUDGET.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : thisMonth.divide(DEFAULT_BUDGET, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        return new Summary(
                thisMonth,
                percentageChange,
                budgetUtil,
                DEFAULT_BUDGET,
                daily,
                dailyChange
        );
    }

    // --- Monthly trends ---

    private List<MonthlyTrend> buildMonthlyTrends(User user, TransactionType type, List<UUID> accountIds,
                                                  LocalDate start,
                                                  LocalDate end) {
        return reportRepository.findMonthlyTrends(user, type, accountIds, start, end)
                .stream()
                .map(p -> new MonthlyTrend(
                        MONTH_LABELS.getOrDefault(p.getMonth(), String.valueOf(p.getMonth())),
                        p.getTotalAmount()
                ))
                .toList();
    }

    private List<CategoryDistribution> buildCategoryDistributions(User user, TransactionType type, List<UUID> accountIds,
                                                                  LocalDate start,
                                                                  LocalDate end) {
        List<CategoryTotalProjection> raw = reportRepository.findCategoryDistributions(user,type, accountIds, start, end);

        BigDecimal grandTotal = raw.stream()
                .map(CategoryTotalProjection::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (grandTotal.compareTo(BigDecimal.ZERO) == 0) return List.of();

        return raw.stream()
                .map(p -> {
                    double pct = p.getTotalAmount()
                            .divide(grandTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    return new CategoryDistribution(p.getCategory(), p.getTotalAmount(), pct);
                })
                .toList();
    }

    // --- Top merchants ---

    private List<TopMerchant> buildTopMerchants(User user, TransactionType type, List<UUID> accountIds,
                                                LocalDate start,
                                                LocalDate end) {
        return reportRepository.findTopMerchants(user, type, accountIds, start, end, TOP_MERCHANTS_LIMIT)
                .stream()
                .map(p -> new TopMerchant(
                        p.getMerchant(),
                        p.getTotalAmount(),
                        p.getTransactionCount(),
                        p.getCategory()
                ))
                .toList();
    }

    // Returns null if there's no previous data to compare against
    private Double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) return null;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }



    // In your service, before calling the repo
    private List<UUID> resolveAccountIds(ReportQueryRequest request, User user) {
        if (request.emailAccountIds() != null && !request.emailAccountIds().isEmpty()) {
            return request.emailAccountIds();
        }
        // "All Accounts" selected — fetch all account IDs for this user
        return emailAccountRepository.findIdsByUser(user);
    }
}



