package com.domain.repositories;


import com.domain.entities.User;
import com.domain.enums.AuthProvider;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User persistence.
 * Used mainly during OAuth login/signup.
 */
public interface UserRepository {

    /**
     * Save or update a user (new signup or profile update).
     */
    User save(User user);

    /**
     * Find by primary email (used in some fallback flows).
     */
    Optional<User> findByEmail(String email);

    /**
     * Primary lookup for OAuth login: provider + providerUserId.
     */
    Optional<User> findByProviderUserIdAndAuthProvider(String providerUserId, AuthProvider authProvider);

    /**
     * Optional: find by ID (for current user context).
     */
    Optional<User> findById(UUID id);
}
