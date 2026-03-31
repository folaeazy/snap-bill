package com.expenseapp.app.dto.expense.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record ExpenseResponse (
        UUID id,

        LocalDate date,
        String displayDate,

        String merchant,
        String merchantLogoUrl,

        BigDecimal amount,
        String currency,
        String formattedAmount,

        String category,

        List<String> tags,

        double aiConfidence,
        String confidenceLabel,

        UUID emailAccountId,
        String emailAccountEmail,

        boolean isEdited,
        String description
) { }
