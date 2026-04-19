package com.expenseapp.app.dto.dashboard.models;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SummaryCards(
        BigDecimal totalSpent,
        double  monthlyChangePercentage,

        BigDecimal thisMonthSpent,
        double thisMonthSpentVsBudgetPercentage,

        String topCategory,
        double topCategoryPercentage,

        int connectedAccounts
) {
}
