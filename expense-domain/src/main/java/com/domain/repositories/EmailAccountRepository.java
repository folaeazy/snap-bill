package com.domain.repositories;


import com.domain.entities.EmailAccount;
import com.domain.entities.User;
import com.domain.enums.AuthProvider;
import com.domain.enums.EmailProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Repository interface for connected email accounts.
 */
public interface EmailAccountRepository {

    /**
     * Save or update a connected email account.
     */
    EmailAccount save(EmailAccount account);

    /**
     * Find by ID.
     */
    Optional<EmailAccount> findById(UUID id);

    /**
     * Find all connected accounts for a user.
     */
    List<EmailAccount> findByUser(User user);

    /**
     * Find by user + provider + email (to avoid duplicates).
     */
    Optional<EmailAccount> findByUserAndProviderAndProviderEmail(
            User user, EmailProvider provider, String providerEmail);

    /**
     * Find accounts that need syncing (e.g. status ACTIVE and lastSyncAt older than threshold).
     */
    List<EmailAccount> findSyncableAccounts();
}
