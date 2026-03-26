package com.infrastructure.persistence.repositories;

import com.domain.entities.TransactionEntity;
import com.domain.repositories.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA implementation of the ExpenseRepository interface.
 * This is the concrete persistence adapter.
 */

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID>, TransactionRepository {
    // The domain interface methods are automatically implemented by Spring Data JPA
}
