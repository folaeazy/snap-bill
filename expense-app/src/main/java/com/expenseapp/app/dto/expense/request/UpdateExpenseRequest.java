package com.expenseapp.app.dto.expense.request;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record UpdateExpenseRequest(
        BigDecimal amount,
        String merchant,
        String category,
        List<String> tags,
        String description,
        LocalDate date
) { }
