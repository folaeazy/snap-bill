package com.infrastructure.persistence.repositories;

import com.domain.entities.RawEmailMessage;
import com.domain.repositories.RawEmailRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataRawEmailMessageRepository extends JpaRepository<RawEmailMessage, UUID>, RawEmailRepository {
}
