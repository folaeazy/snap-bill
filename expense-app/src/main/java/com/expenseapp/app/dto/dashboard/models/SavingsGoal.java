package com.expenseapp.app.dto.dashboard.models;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SavingsGoal(
        String name,
        BigDecimal currentAmount,
        BigDecimal targetAmount,
        double progressPercentage
) {
}
