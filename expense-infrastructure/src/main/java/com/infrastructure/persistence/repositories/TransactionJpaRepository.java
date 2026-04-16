package com.infrastructure.persistence.repositories;

import com.domain.entities.TransactionEntity;
import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.interfaces.projections.MerchantProjection;
import com.domain.interfaces.projections.MonthlyTrendProjection;
import com.domain.interfaces.projections.RecentExpenseProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * JPA implementation of the ExpenseRepository interface.
 * This is the concrete persistence adapter.
 */

@Repository
interface TransactionJpaRepository extends
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


    @Query("""
            SELECT t.category as category, SUM(t.amount) as totalAmount
                FROM TransactionEntity t
                WHERE t.user = :user
                  AND t.type = :type
                GROUP BY t.category
                ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryTotalProjection> findTopCategories(@Param("user") User user, @Param("type") TransactionType type);


    @Query("""
        SELECT\s
        t.id as id,
        t.transactionDateTime as transactionDateTime,
        t.merchant as merchantName,
        t.category as categoryName,
        t.amount as amount,
        t.currency as currency
    FROM TransactionEntity t
    WHERE t.user = :user
      AND t.type = :type
    ORDER BY t.transactionDateTime DESC
           \s""")
    List<RecentExpenseProjection> findRecentExpenses(
            @Param("user") User user,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    // Sum total Amount by accounts and date range
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
                    FROM TransactionEntity t
                    WHERE t.user = :user
                      AND t.type = :type
                      AND t.emailAccount.id IN :accountIds
                      AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumAmountByAccounts(@Param("user") User user,
                                   @Param("type") TransactionType type,
                                   @Param("accountIds") List<UUID> accountIds,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end
                                   );

    // Monthly Trend

    @Query("""
            SELECT
                    YEAR(t.transactionDate)  AS year,
                    MONTH(t.transactionDate) AS month,
                    SUM(t.amount)            AS totalAmount
                FROM TransactionEntity t
                WHERE t.user = :user
                  AND t.type = :type
                  AND t.transactionDate BETWEEN :start AND :end
                  AND t.emailAccount.id IN :accountIds
                GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate)
                ORDER BY YEAR(t.transactionDate) ASC, MONTH(t.transactionDate) ASC
            """)
    List<MonthlyTrendProjection> findMonthlyTrends(@Param("user") User user,
                                                   @Param("type") TransactionType type,
                                                   @Param("accountIds") List<UUID> accountIds,
                                                   @Param("start") LocalDate start,
                                                   @Param("end") LocalDate end);


    // Category distribution

    @Query("""
            SELECT
                  t.category  AS category,
                 SUM(t.amount) AS totalAmount
            FROM TransactionEntity t
            WHERE t.user = :user
                  AND t.type = :type
                  AND t.emailAccount.id IN :accountIds
                  AND t.transactionDate BETWEEN :start AND :end
            GROUP BY t.category
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryTotalProjection> findCategoryDistributions(@Param("user") User user,
                                                            @Param("type") TransactionType type,
                                                            @Param("accountIds") List<UUID> accountIds,
                                                            @Param("start") LocalDate start,
                                                            @Param("end") LocalDate end);


    // Top merchant distribution
    @Query("""
        SELECT
            t.merchant          AS merchant,
            t.category          AS category,
            SUM(t.amount)       AS totalAmount,
            COUNT(t.id)         AS transactionCount
        FROM TransactionEntity t
        WHERE t.user = :user
          AND t.type = :type
          AND t.emailAccount.id IN :accountIds
          AND t.transactionDate BETWEEN :start AND :end
        GROUP BY t.merchant, t.category
        ORDER BY SUM(t.amount) DESC
        LIMIT :limit
    """)
    List<MerchantProjection> findTopMerchants(@Param("user") User user,
                                              @Param("type") TransactionType type,
                                              @Param("accountIds") List<UUID> accountIds,
                                              @Param("start") LocalDate start,
                                              @Param("end") LocalDate end,
                                              @Param("limit") int limit);

}


