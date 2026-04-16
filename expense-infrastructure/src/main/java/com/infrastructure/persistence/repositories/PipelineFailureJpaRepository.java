package com.infrastructure.persistence.repositories;

import com.domain.entities.PipelineFailure;
import com.domain.repositories.PipelineFailureRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA implementation of the UserRepository interface.
 * This is the concrete persistence adapter.
 */
@Repository
interface PipelineFailureJpaRepository extends JpaRepository<PipelineFailure, UUID>, PipelineFailureRepository {
    // The domain interface methods are automatically implemented by Spring Data JPA
}
