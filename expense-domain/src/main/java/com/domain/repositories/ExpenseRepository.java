package com.domain.repositories;


import com.domain.entities.Expense;
import com.domain.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for Expense persistence operations.
 * Domain layer only — no JPA/Spring Data here.
 * Implementations live in expense-infrastructure (e.g. JpaExpenseRepository).
 */
public interface ExpenseRepository {

    /**
     * Save or update an expense.
     */
    Expense save(Expense expense);

    /**
     * Find by internal ID.
     */
    Optional<Expense> findById(UUID id);

    /**
     * Find all expenses for a user within a date range.
     */
    List<Expense> findByUserIdAndDateRange(UUID userId, LocalDate start, LocalDate endInclusive);

    /**
     * Find debits (expenses) only for a user in a date range.
     */
    List<Expense> findDebitsByUserIdAndDateRange(UUID userId, LocalDate start, LocalDate endInclusive);

    /**
     * Find expenses by type (e.g. all DEBIT for dashboard totals).
     */
    List<Expense> findByUserIdAndType(UUID userId, TransactionType type);

    /**
     * Optional: find by merchant name (for recurring detection or search).
     */
    List<Expense> findByUserIdAndMerchantNameContaining(UUID userId, String merchantName);

    /**
     * Count expenses in a period (for quick summaries without loading full list).
     */
    long countByUserIdAndDateRange(UUID userId, LocalDate start, LocalDate endInclusive);

    /**
     * Delete an expense (soft or hard — depends on your business rules).
     */
    void deleteById(UUID id);
}
