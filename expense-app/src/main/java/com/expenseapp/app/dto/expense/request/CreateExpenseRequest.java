package com.expenseapp.app.dto.expense.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record CreateExpenseRequest(
        BigDecimal amount,
        String currency,
        String merchant,
        String category,
        List<String> tags,
        String description,
        LocalDate date,
        UUID emailAccountId

) { }
