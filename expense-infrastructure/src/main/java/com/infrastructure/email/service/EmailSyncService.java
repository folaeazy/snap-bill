package com.infrastructure.email.service;


import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.gateways.EmailGateway;
import com.domain.model.EmailMessage;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.RawEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSyncService {

    /**
     * Spring auto-injects all EmailGateway beans into this map.
     * Keys are bean names like "gmailEmailGateway", "outlookEmailGateway".
     */
    private final Map<String, EmailGateway> emailGateways;
    private final RawEmailRepository rawEmailRepository;
    private final EmailAccountRepository emailAccountRepository;

    /**
     * Sync one email account (called by scheduler or manual trigger).
     *
     * @param account The connected email account to sync
     * @return Number of new expenses successfully processed and saved
     */

    public int syncAccount(EmailAccount account) {
        String provider = account.getProvider().toString().toLowerCase();
        log.info("{} is the provider............!!", provider.toLowerCase());
        String gatewayKey = provider + "EmailGateway";

        EmailGateway gateway = emailGateways.get(gatewayKey);
        if (gateway == null) {
            log.error("No EmailGateway found for provider: {}", provider);
            return 0;
        }

        // Determine starting point for fetch
        Instant since = account.getLastSyncAt();

        log.info("First sync for {} - fetching from {}", account.getProviderEmail(), since);

        try {
            // Fetch new messages since last sync
            List<RawEmailMessage> messages = gateway.fetchNewMessages(account, since);
            if(messages.isEmpty()){
                log.info("No new messages for {}", account.getProviderEmail());
                return 0;
            }

            //save raw emails for processing
            rawEmailRepository.save(messages);

            Instant newestEmail = messages.stream()
                    .map(RawEmailMessage::getReceivedDate)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());

            account.setLastSyncAt(newestEmail);

            emailAccountRepository.save(account);


            // TODO: Store + process
            log.info("Updated lastSyncAt for {} to {}", account.getProviderEmail(), account.getLastSyncAt());
            return messages.size();

        }catch (Exception e) {
            log.error("Sync failed for {}: {}", account.getProviderEmail(), e.getMessage(), e);
            return 0;
        }
    }


    /**
     * Sync all active email accounts for a user (called by scheduler or user trigger).
     *
     * @param userId The user whose accounts to sync
     * @return Total new expenses processed across all accounts
     */
    public int syncAllForUser(UUID userId) {
        // TODO: load all active EmailAccount for userId
        // List<EmailAccount> accounts = emailAccountRepository.findActiveByUserId(userId);

        // Placeholder: assume one account for testing
        EmailAccount account = new EmailAccount(); // replace with real load

        return syncAccount(account);
    }
}
