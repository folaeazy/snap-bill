package com.expenseapp.app.dto.report.models;

import java.math.BigDecimal;

public record TopMerchant(
        String merchant,
        BigDecimal totalAmount,
        int transactionCount,
        String category
) {
}
