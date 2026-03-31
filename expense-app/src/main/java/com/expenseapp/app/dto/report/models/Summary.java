package com.expenseapp.app.dto.report.models;

import java.math.BigDecimal;

public record Summary(
        BigDecimal totalSpentThisMonth,
        double percentageChange,

        double budgetUtilization,   // 72
        BigDecimal budgetAmount,    // 6000

        BigDecimal dailyAverage,
        double dailyAverageChange
) {
}
