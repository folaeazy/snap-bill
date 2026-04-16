package com.domain.interfaces.projections;

import java.math.BigDecimal;

public interface MonthlyTrendProjection {
    BigDecimal getTotalAmount();
    String getMerchant();
    String getCategory();
    int getTransactionCount();

    boolean getMonth();
}
