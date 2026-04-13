package com.infrastructure.persistence.repositories;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.model.PagedResponse;
import com.domain.repositories.RawEmailRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRawEmailRepository extends JpaRepository<RawEmailMessage, UUID> {

    //@Override
    default List<RawEmailMessage> saveAllMessages(Iterable<RawEmailMessage> messages) {
        return saveAll(messages);
    }


    @Modifying
    @Query("""
            UPDATE RawEmailMessage e
            SET e.processed = 'PROCESSING',
                e.processingStartedAt = :now,
                e.claimToken = :token
            WHERE e.id IN :ids
            """)
    int claimByIds(List<UUID> id, Instant now, UUID token);

    @Query("""
            SELECT e FROM RawEmailMessage e
            JOIN FETCH e.emailAccount acc
            JOIN FETCH acc.user
            WHERE e.claimToken = :token
            """)
    List<RawEmailMessage> findByClaimToken(UUID token);


    /**
     *
     * Query to fetch ids in batch specified per account
     */
    @Query("""
            SELECT e.id FROM RawEmailMessage e
            WHERE e.emailAccount.id = :accountId
            AND (
                e.processed = 'PENDING'
                OR (e.processed = 'PROCESSING' AND e.processingStartedAt < :timeout)
            )
            AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
            AND e.retryCount < :maxRetry
            ORDER BY e.receivedDate ASC
            """)
    List<UUID> findIdsForClaimInternal(
            UUID accountId,
            Instant timeout,
            Instant now,
            int maxRetry,
            Pageable pageable
    );


    List<RawEmailMessage> findByEmailAccount(EmailAccount emailAccount);

    void deleteByEmailAccount(EmailAccount emailAccount);
}
