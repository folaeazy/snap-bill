package com.expenseapp.app.dto.dashboard.models;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SummaryCards(
        BigDecimal totalSpent,
        double totalSpentChangePercentage,

        BigDecimal thisMonthSpent,
        double monthlyChangePercentage,

        String topCategory,
        double topCategoryPercentage,

        int connectedAccounts
) {
}
