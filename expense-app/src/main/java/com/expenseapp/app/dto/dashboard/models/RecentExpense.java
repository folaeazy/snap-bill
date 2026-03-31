package com.expenseapp.app.dto.dashboard.models;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record RecentExpense(
        UUID id,
        LocalDate date,
        String displayDate,

        String merchant,
        String category,

        BigDecimal amount,
        String currency
) {
}
