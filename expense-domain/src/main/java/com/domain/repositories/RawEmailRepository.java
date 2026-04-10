package com.domain.repositories;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.enums.ProcessingStatus;
import com.domain.model.PagedResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for Expense persistence operations.
 * Domain layer only — no JPA/Spring Data here.
 * Implementations live in expense-infrastructure).
 */
public interface RawEmailRepository {

    /**
     * Find all raw emails for a specific EmailAccount.
     * Useful for re-processing or auditing.
     */

    RawEmailMessage save(RawEmailMessage message);

    List<RawEmailMessage> saveAllMessages(Iterable<RawEmailMessage> messages);

    Optional<RawEmailMessage> findById(UUID id);

    List<RawEmailMessage> findByEmailAccount(EmailAccount emailAccount);
    //List<RawEmailMessage> findTop50ByProcessedOrderByReceivedDateAsc(ProcessingStatus status);

    // Additional methods if needed (e.g. deleteByEmailAccount)
    void deleteByEmailAccount(EmailAccount emailAccount);

    List<RawEmailMessage> findTopByEmailAccountAndProcessedOrderByReceivedDateAsc(UUID accountId, ProcessingStatus processingStatus, int batchSize);

    int claimEmail(UUID id, Instant now, Instant timeout);

    int claimBatchForAccount(UUID accountId, Instant now, Instant timeout, int maxRetry);

    List<RawEmailMessage> findClaimedBatchForAccount(
            UUID accountId,
            Instant now,
            PagedResponse pageable
    );
}
