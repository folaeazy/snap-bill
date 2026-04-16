package com.domain.interfaces.projections;

import java.math.BigDecimal;

public interface CategoryTotalProjection {
    String getCategory();
    BigDecimal getTotalAmount();

}
