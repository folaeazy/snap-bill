package com.domain.interfaces.projections;

import com.domain.valueObjects.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecentExpenseProjection {
        UUID getId();

        Instant getTransactionDateTime();

        String getMerchant();

        String getCategory();

        BigDecimal getAmount();

        CurrencyCode getCurrency();

}
