package com.expenseapp.app.service;

import com.domain.entities.User;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.interfaces.projections.RecentExpenseProjection;
import com.domain.repositories.DashboardRepository;
import com.domain.repositories.EmailAccountRepository;
import com.expenseapp.app.dto.dashboard.models.AiInsight;
import com.expenseapp.app.dto.dashboard.models.RecentExpense;
import com.expenseapp.app.dto.dashboard.models.SavingsGoal;
import com.expenseapp.app.dto.dashboard.models.SummaryCards;
import com.expenseapp.app.dto.dashboard.response.DashboardResponse;
import com.expenseapp.app.interfaces.DashboardService;
import com.expenseapp.app.util.DateFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepositoryRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final DateFormatter dateFormatter;

    /**
     * @return Dashboard response DTO
     */
    @Override
    public DashboardResponse getDashboardSummary(User user) {
        return DashboardResponse.builder()
                .summary(buildSummary(user))
                .userName(user.getName())
                .aiInsight(buildAiInsight())
                .recentExpenses(getRecentExpenses(user))
                .savingsGoal(buildSavingsGoal())
                .build();

    }






    /**
       =================Helper methods ================
     */

    // ========================
    // RECENT EXPENSES
    // ========================
    private List<RecentExpense> getRecentExpenses(User user) {
        List<RecentExpenseProjection> result = dashboardRepositoryRepository.findRecentExpenses(
                user,
                TransactionType.DEBIT,
                5
        );

        return result.stream()
                .map(this::mapToRecentExpense)
                .toList();

    }


    private RecentExpense mapToRecentExpense(RecentExpenseProjection recents) {

        Instant date = recents.getTransactionDateTime();
        LocalDate localDate = LocalDate.parse(dateFormatter.formatDate(date)); //TODO: watch!!!
        return RecentExpense.builder()
                .id(recents.getId())
                .date(localDate)
                .merchant(recents.getMerchant())
                .displayDate(dateFormatter.formatDate(recents.getTransactionDateTime()))
                .category(recents.getCategory())
                .currency(recents.getCurrency().name())
                .amount(recents.getAmount())
                .build();
    }

    // ========================
    // SUMMARY CARD
    // ========================
    private SummaryCards buildSummary(User user) {


        LocalDate now = LocalDate.now();

        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now;

        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        //  Total spent (all time)
        BigDecimal totalSpent =
                dashboardRepositoryRepository.getTotalSpent(user, TransactionType.DEBIT);

        //  This month
        BigDecimal thisMonthSpent =
                dashboardRepositoryRepository.getSpentBetween(
                        user,
                        TransactionType.DEBIT,
                        startOfMonth,
                        endOfMonth
                );

        //  Last month
        BigDecimal lastMonthSpent =
                dashboardRepositoryRepository.getSpentBetween(
                        user,
                        TransactionType.DEBIT,
                        startOfLastMonth,
                        endOfLastMonth
                );

        //  Percentage calculations
        double monthlyChange =
                calculatePercentageChange(thisMonthSpent, lastMonthSpent);

        // simple version for now

        //  Top category
        List<CategoryTotalProjection> categories =
                dashboardRepositoryRepository.findTopCategories(user, TransactionType.DEBIT);

        String topCategory = "N/A";
        double topCategoryPercentage = 0;

        if (!categories.isEmpty()) {
            CategoryTotalProjection top = categories.getFirst();
            topCategory = top.getCategory();

            if (totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                topCategoryPercentage = top.getTotalAmount()
                        .divide(totalSpent, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
        }

        //  Connected accounts
        int connectedAccounts =
                emailAccountRepository.countByUserAndStatus(
                        user,
                        ConnectionStatus.ACTIVE
                );

        return SummaryCards.builder()
                .totalSpent(totalSpent)
                .thisMonthSpentVsBudgetPercentage(-10.00) // Todo: implement later
                .thisMonthSpent(thisMonthSpent)
                .monthlyChangePercentage(monthlyChange)
                .topCategory(topCategory)
                .topCategoryPercentage(topCategoryPercentage)
                .connectedAccounts(connectedAccounts)
                .build();
    }

    // ========================
    // AI INSIGHT
    // ========================
    private AiInsight buildAiInsight() {
        return AiInsight.builder()
                .type("info")
                .message("Your spending is stable this month.")
                .build();
    }

    private SavingsGoal buildSavingsGoal() {
        return SavingsGoal.builder()
                .name("Emergency Fund")
                .currentAmount(BigDecimal.valueOf(150000)) //Todo: implement later
                .targetAmount(BigDecimal.valueOf(500000)) // Todo : implement later
                .progressPercentage(30.0)
                .build();
    }

    // ((current - previous) / previous ) * 100
    private double calculatePercentageChange(BigDecimal current, BigDecimal previous) {

        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }




}
