package com.infrastructure.persistence.repositories;

import com.domain.entities.TransactionEntity;
import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.repositories.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA implementation of the ExpenseRepository interface.
 * This is the concrete persistence adapter.
 */

@Repository
public interface SpringDataTransactionRepository extends
        JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

    // The domain interface methods are automatically implemented by Spring Data JPA

    @Query("""
    
        SELECT COALESCE(SUM(t.amount), 0)
        FROM TransactionEntity t
        WHERE t.user = :user
          AND t.type = :type
    """)
    BigDecimal getTotalSpent(@Param("user") User user, @Param("type") TransactionType type);


    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM TransactionEntity t
        WHERE t.user = :user
          AND t.type = :type
          AND t.transactionDate BETWEEN :start AND :end
    """)
    BigDecimal getSpentBetween(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
