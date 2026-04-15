package com.domain.interfaces;

import com.domain.valueObjects.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface RecentExpenseProjection {
        UUID getId();

        Instant getTransactionDateTime();

        String getMerchantName();

        String getCategoryName();

        BigDecimal getAmount();

        CurrencyCode getCurrency();

}
