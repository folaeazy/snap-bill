package com.infrastructure.persistence.repositories;

import com.domain.entities.RawEmailMessage;
import com.domain.repositories.RawEmailRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRawEmailRepository extends JpaRepository<RawEmailMessage, UUID>, RawEmailRepository {
    @Override
    default List<RawEmailMessage> saveAllMessages(Iterable<RawEmailMessage> messages) {
        return saveAll(messages);
    }

    // For single email claim (used in emergency/manual cases)
    @Modifying
    @Query("""
        UPDATE RawEmailMessage e 
        SET e.processed = 'PROCESSING',
            e.processingStartedAt = :now
        WHERE e.id = :id 
          AND (e.processed = 'PENDING' 
               OR (e.processed = 'PROCESSING' AND e.processingStartedAt < :timeout))
    """)
    int claimEmail(UUID id, Instant now, Instant timeout);

    // Batch claim (this is what we will use most of the time)
    @Modifying
    @Query("""
        UPDATE RawEmailMessage e 
        SET e.processed = 'PROCESSING',
            e.processingStartedAt = :now
        WHERE e.emailAccount.id = :accountId 
          AND (e.processed = 'PENDING' 
               OR (e.processed = 'PROCESSING' AND e.processingStartedAt < :timeout))
          AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
          AND e.retryCount < :maxRetry
    """)
    int claimBatchForAccount(UUID accountId, Instant now, Instant timeout, int maxRetry);


    // Custom query for fetching with eager loading
    @Query("""
        SELECT e FROM RawEmailMessage e
        JOIN FETCH e.emailAccount ea
        JOIN FETCH ea.user
        WHERE ea.id = :accountId
        AND (e.processed = 'PROCESSING')  -- we only fetch what we just claimed
        AND e.processingStartedAt = :now   -- safety check
        ORDER BY e.receivedDate ASC
    """)
    List<RawEmailMessage> findClaimedBatchForAccount(
            UUID accountId,
            Instant now,
            Pageable pageable
    );

}
