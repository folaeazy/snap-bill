package com.domain.model;

import com.domain.valueObjects.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;

public record ExtractionResult(
        String merchant,
        BigDecimal amount,
        CurrencyCode currency,
        String category,
        String transactionDate,
        boolean transaction

) { }
