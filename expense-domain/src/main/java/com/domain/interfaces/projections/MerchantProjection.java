package com.domain.interfaces.projections;

import java.math.BigDecimal;

public interface MerchantProjection {
    String getMerchant();
    String getCategory();
    BigDecimal getTotalAmount();
    int getTransactionCount();
}
