package com.expenseapp.app.dto.report.models;

import java.math.BigDecimal;

public record CategoryDistribution(
        String category,
        BigDecimal amount,
        double percentage
) {
}
