package com.domain.repositories;

import com.domain.entities.Transaction;

public interface TransactionRepository {
    void save(Transaction transaction);
}
