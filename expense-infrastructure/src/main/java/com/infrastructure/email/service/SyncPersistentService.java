package com.infrastructure.email.service;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.RawEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Persist RawEmail into its transaction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncPersistentService {

    private final RawEmailRepository rawEmailRepository;
    private final EmailAccountRepository emailAccountRepository;

    @Transactional
    public void persistSyncResults(EmailAccount account, List<RawEmailMessage> messages) {
        rawEmailRepository.saveAllMessages(messages);
        Instant newestEmail = messages.stream()
                .map(RawEmailMessage::getReceivedDate)
                .max(Instant::compareTo)
                .orElse(Instant.now());

        account.setLastEmailReceivedAt(newestEmail);

        emailAccountRepository.save(account);
        log.info("Updated lastSyncAt for {} to {}", account.getProviderEmail(), account.getLastSyncAt());
    }
}
