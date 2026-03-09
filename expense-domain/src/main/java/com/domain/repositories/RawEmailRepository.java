package com.domain.repositories;

import com.domain.entities.RawEmailMessage;

import java.util.List;

/**
 * Repository interface (port) for Expense persistence operations.
 * Domain layer only — no JPA/Spring Data here.
 * Implementations live in expense-infrastructure).
 */
public interface RawEmailRepository {
    ;

    void save(List<RawEmailMessage> messages);
}
