package com.domain.repositories;

import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.interfaces.projections.MerchantProjection;
import com.domain.interfaces.projections.MonthlyTrendProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportRepository {

    /**
     * total amount of expenses per list of account between date range
     */
    BigDecimal sumAmountByAccounts(User user, TransactionType type, List<UUID> accountIds, LocalDate start, LocalDate end);

    /**
     * Monthly spend projection
     */
    List<MonthlyTrendProjection> findMonthlyTrends(User user, TransactionType type, List<UUID> accountIds, LocalDate start, LocalDate end);

    /**
     * Find category distribution
     */
    List<CategoryTotalProjection> findCategoryDistributions(User user, TransactionType type,List<UUID> accountIds, LocalDate start, LocalDate end);


    /**
     * Find top merchant in the expenses
     */
    List<MerchantProjection> findTopMerchants(User user, TransactionType type,List<UUID> accountIds, LocalDate start, LocalDate end, int limit);

}
