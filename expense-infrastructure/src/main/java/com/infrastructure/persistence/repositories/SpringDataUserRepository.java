package com.infrastructure.persistence.repositories;

import com.domain.entities.User;
import com.domain.repositories.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
/**
 * JPA implementation of the UserRepository interface.
 * This is the concrete persistence adapter.
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<User, UUID>, UserRepository{
    // The domain interface methods are automatically implemented by Spring Data JPA
}
