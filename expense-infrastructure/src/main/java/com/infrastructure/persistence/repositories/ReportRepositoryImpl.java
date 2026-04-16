package com.infrastructure.persistence.repositories;

import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.interfaces.projections.MerchantProjection;
import com.domain.interfaces.projections.MonthlyTrendProjection;
import com.domain.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {

    private final TransactionJpaRepository jpASpr;


    /**
     * @param user : authenticated user
     * @param type transaction type of debit
     * @param accountIds list of email accounts
     * @param start      of the date range
     * @param end        of the date range
     * @return BigDecimal sum of amounts of accounts
     */
    @Override
    public BigDecimal sumAmountByAccounts(User user, TransactionType type, List<UUID> accountIds, LocalDate start, LocalDate end) {
        return jpASpr.sumAmountByAccounts(user, type, accountIds, start, end);
    }

    /**
     * @param user : authenticated user
     * @param type transaction type of debit
     * @param accountIds list of email accounts
     * @param start      of the date range
     * @param end        of the date range
     * @return list of monthly trend
     */
    @Override
    public List<MonthlyTrendProjection> findMonthlyTrends(User user, TransactionType type, List<UUID> accountIds, LocalDate start, LocalDate end) {
        return jpASpr.findMonthlyTrends(user, type, accountIds, start, end);
    }

    /**
     * @param user : authenticated user
     * @param type transaction type of debit
     * @param accountIds list of email accounts
     * @param start      of the date range
     * @param end        of the date range
     * @return list of category distributions
     */
    @Override
    public List<CategoryTotalProjection> findCategoryDistributions(User user, TransactionType type, List<UUID> accountIds, LocalDate start, LocalDate end) {
        return jpASpr.findCategoryDistributions(user, type, accountIds, start, end);
    }

    /**
     * @param user : authenticated user
     * @param type transaction type of debit
     * @param accountIds list of email accounts
     * @param start      of the date range
     * @param end        of the date range
     * @param limit      the amount to be fetched
     * @return list of merchant distributions
     */
    @Override
    public List<MerchantProjection> findTopMerchants(User user, TransactionType type, List<UUID> accountIds, LocalDate start, LocalDate end, int limit) {
        return jpASpr.findTopMerchants(user, type, accountIds, start, end, limit);
    }

}
