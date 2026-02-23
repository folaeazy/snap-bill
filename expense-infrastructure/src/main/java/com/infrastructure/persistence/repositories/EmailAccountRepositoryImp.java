package com.infrastructure.persistence.repositories;

import com.domain.entities.EmailAccount;
import com.domain.repositories.EmailAccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
/**
 * JPA implementation of the EmailAccountRepository interface.
 * This is the concrete persistence adapter.
 */
@Repository
public interface EmailAccountRepositoryImp extends JpaRepository<EmailAccount, UUID>, EmailAccountRepository {
    // The domain interface methods are automatically implemented by Spring Data JPA
}
