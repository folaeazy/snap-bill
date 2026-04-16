package com.domain.repositories;

import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.interfaces.projections.RecentExpenseProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DashboardRepository {

    /**
     * Get total spent in through debit transaction
     */
    BigDecimal getTotalSpent(User user, TransactionType type);


    /**
     * Find all transaction for a user within start and end date
     */
    BigDecimal getSpentBetween(User user, TransactionType type, LocalDate start, LocalDate end);

    /**
     *
     * Find all top categories
     */
    List<CategoryTotalProjection> findTopCategories(User user, TransactionType type);

    /**
     * Find recent expenses for user with limit
     */
    List<RecentExpenseProjection> findRecentExpenses(User user, TransactionType type, int limit);


}
