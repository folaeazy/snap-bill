package com.infrastructure.persistence.repositories;

import com.domain.entities.RawEmailMessage;
import com.domain.repositories.RawEmailRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRawEmailRepository extends JpaRepository<RawEmailMessage, UUID>, RawEmailRepository {
    @Override
    default List<RawEmailMessage> saveAllMessages(Iterable<RawEmailMessage> messages) {
        return saveAll(messages);
    }
}
