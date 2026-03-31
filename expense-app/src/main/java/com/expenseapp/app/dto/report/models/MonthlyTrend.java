package com.expenseapp.app.dto.report.models;

import java.math.BigDecimal;

public record MonthlyTrend(
        String month,   //JAN, FEB
        BigDecimal totalAmount
) {
}
