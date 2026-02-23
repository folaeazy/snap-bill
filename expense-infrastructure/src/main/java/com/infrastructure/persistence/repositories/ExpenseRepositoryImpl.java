package com.infrastructure.persistence.repositories;

import com.domain.entities.Expense;
import com.domain.repositories.ExpenseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA implementation of the ExpenseRepository interface.
 * This is the concrete persistence adapter.
 */

@Repository
public interface ExpenseRepositoryImpl extends JpaRepository<Expense, UUID>, ExpenseRepository {
    // The domain interface methods are automatically implemented by Spring Data JPA
}
