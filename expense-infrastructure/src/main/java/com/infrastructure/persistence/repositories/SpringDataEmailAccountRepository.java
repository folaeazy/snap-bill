package com.infrastructure.persistence.repositories;

import com.domain.entities.EmailAccount;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.EmailProvider;
import com.domain.repositories.EmailAccountRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
/**
 * JPA implementation of the EmailAccountRepository interface.
 * This is the concrete persistence adapter.
 */
@Repository
public interface SpringDataEmailAccountRepository extends JpaRepository<EmailAccount, UUID>, EmailAccountRepository {
    // The domain interface methods are automatically implemented by Spring Data JPA
    @Query("""
    SELECT e
    FROM EmailAccount e
    WHERE e.status = :status
    AND (e.lastSyncAt IS NULL OR e.lastSyncAt < :threshold)
    ORDER BY e.lastSyncAt ASC
    """)
    List<EmailAccount> findAccountsToSync(@Param("status") ConnectionStatus status,@Param("threshold") Instant threshold);

}
