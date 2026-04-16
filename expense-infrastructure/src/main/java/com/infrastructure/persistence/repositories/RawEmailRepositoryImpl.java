package com.infrastructure.persistence.repositories;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.repositories.RawEmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RawEmailRepositoryImpl implements RawEmailRepository {
    private final RawEmailJpaRepository jpaRep;

    @Override
    public RawEmailMessage saveMessage(RawEmailMessage message) {
        return jpaRep.save(message);
    }

    @Override
    public List<RawEmailMessage> saveAllMessages(Iterable<RawEmailMessage> messages) {
        return jpaRep.saveAllMessages(messages);
    }

    @Override
    public Optional<RawEmailMessage> findByUid(UUID id) {
        return jpaRep.findById(id);
    }

    @Override
    public List<RawEmailMessage> findByEmailAccount(EmailAccount emailAccount) {
        return jpaRep.findByEmailAccount(emailAccount);
    }

    @Override
    public void deleteByEmailAccount(EmailAccount emailAccount) {
        jpaRep.deleteByEmailAccount(emailAccount);

    }


    @Override
    public int claimByIds(List<UUID> id, Instant now, UUID token) {
        return jpaRep.claimByIds(id, now, token);
    }


    @Override
    public List<RawEmailMessage> findByClaimToken(UUID token) {
        return jpaRep.findByClaimToken(token);
    }

    @Override
    public List<UUID> findIdsForClaim(UUID accountId, Instant timeout, Instant now, int maxRetry, int limit) {
        return jpaRep.findIdsForClaimInternal(
                accountId,
                timeout,
                now,
                maxRetry,
                PageRequest.of(0, limit)
        );
    }
}
