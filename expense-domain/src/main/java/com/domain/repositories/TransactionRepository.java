package com.domain.repositories;

import com.domain.entities.TransactionEntity;
import com.domain.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {


    /**
     * Save or update an expense.
     */
    TransactionEntity save(TransactionEntity expense);

    /**
     * Find by internal ID.
     */
    Optional<TransactionEntity> findById(UUID id);

    /**
     * Find all expenses for a user within a date range.
     */
    List<TransactionEntity> findByUserIdAndTransactionDateBetween(UUID userId, LocalDate start, LocalDate endInclusive);

    /**
     * Find debits (expenses) only for a user in a date range.
     */
    List<TransactionEntity> findDebitsByUserIdAndTransactionDateBetween(UUID userId, LocalDate start, LocalDate endInclusive);

    /**
     * Find expenses by type (e.g. all DEBIT for dashboard totals).
     */
    List<TransactionEntity> findByUserIdAndType(UUID userId, TransactionType type);

    /**
     * Optional: find by merchant name (for recurring detection or search).
     */
    List<TransactionEntity> findByUserIdAndMerchantContaining(UUID userId, String merchantName);

    /**
     * Count expenses in a period (for quick summaries without loading full list).
     */
    long countByUserIdAndTransactionDateBetween(UUID userId, LocalDate start, LocalDate endInclusive);

    /**
     * Delete an expense (soft or hard — depends on your business rules).
     */
    void deleteById(UUID id);
}
