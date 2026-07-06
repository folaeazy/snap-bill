package com.infrastructure.email.service;

import com.domain.repositories.EmailAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * A dedicated service for claiming and releasing
 * claim must commit independently
 * each method with its own transaction
 */
@Service
@RequiredArgsConstructor
public class SyncLockService {

    private final EmailAccountRepository emailAccountRepository;


    // returns its own transaction - commits independently
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryClaim(UUID accountId) {
        int claimed = emailAccountRepository.tryClaimForSync(accountId);
        return claimed == 1;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(UUID accountId) {
        emailAccountRepository.releaseSyncLock(accountId);
    }
}
