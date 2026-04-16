package com.infrastructure.persistence.repositories;

import com.domain.entities.User;
import com.domain.enums.TransactionType;
import com.domain.interfaces.projections.CategoryTotalProjection;
import com.domain.interfaces.projections.RecentExpenseProjection;
import com.domain.repositories.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {

    private final TransactionJpaRepository jpASpr;

    @Override
    public BigDecimal getTotalSpent(User user, TransactionType type) {
        return jpASpr.getTotalSpent(user,type);
    }

    @Override
    public BigDecimal getSpentBetween(User user, TransactionType type, LocalDate start, LocalDate end) {
        return jpASpr.getSpentBetween(user, type, start, end);
    }


    @Override
    public List<CategoryTotalProjection> findTopCategories(User user, TransactionType type) {
        return jpASpr.findTopCategories(user, type);
    }


    @Override
    public List<RecentExpenseProjection> findRecentExpenses(User user, TransactionType type, int limit) {
        return jpASpr.findRecentExpenses(user, type, PageRequest.of(0,limit));
    }
}
