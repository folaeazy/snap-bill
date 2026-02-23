package com.domain.model;

import com.domain.enums.TransactionType;
import com.domain.valueObjects.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ParsedExpense(
        TransactionType type,
        BigDecimal amount,
        CurrencyCode currency,
        LocalDate date,
        String merchantName,
        String categoryName,
        Set<String> tags,
        String description,
        double AiConfidence
) {
}
