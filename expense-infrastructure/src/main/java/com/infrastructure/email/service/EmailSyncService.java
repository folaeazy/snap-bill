package com.infrastructure.email.service;


import com.domain.entities.EmailAccount;
import com.domain.gateways.EmailGateway;
import com.domain.model.EmailMessage;
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

    /**
     * Sync one email account (called by scheduler or manual trigger).
     *
     * @param account The connected email account to sync
     * @return Number of new expenses successfully processed and saved
     */

    public int syncAccount(EmailAccount account) {
        String provider = account.getProvider().toString();
        String gatewayKey = provider + "EmailGateway";

        EmailGateway gateway = emailGateways.get(gatewayKey);
        if (gateway == null) {
            log.error("No EmailGateway found for provider: {}", provider);
            return 0;
        }

        // Determine starting point for fetch
        Instant since = account.getLastSyncAt();
        if (since == null) {
            // First sync: last 30 days (adjust as needed)
            since = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
            log.info("First sync for {} - fetching from {}", account.getProviderEmail(), since);
        }
        try {
            // Fetch new messages since last sync
            List<EmailMessage> messages = gateway.fetchNewMessages(account, since);

            int savedCount = 0;

            for (EmailMessage msg : messages) {
                // TODO: quick pre-filter here (optional, to skip obvious junk)
                // e.g. if (!isLikelyTransaction(msg)) continue;

                // Classify & extract (placeholder – real impl next)
                // ParsedExpense parsed = aiClassificationService.classifyAndExtract(msg);
                // if (parsed != null && parsed.isValidExpense()) {
                //     expenseService.createExpenseFromParsed(parsed);
                //     savedCount++;
                // }

                // For now: just log
                log.debug("Processed message: {} from {}", msg.getSubject(), msg.getFrom());
                savedCount++; // temp counter
            }

            // If at least one message processed successfully → update checkpoint
            if (!messages.isEmpty()) {
                account.setLastSyncAt(Instant.now());
                // TODO: save account via EmailAccountRepository
                // emailAccountRepository.save(account);
                log.info("Updated lastSyncAt for {} to {}", account.getProviderEmail(), account.getLastSyncAt());
            }

            return savedCount;

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
