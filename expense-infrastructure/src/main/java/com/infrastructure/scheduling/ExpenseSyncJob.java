package com.infrastructure.scheduling;


import com.domain.entities.EmailAccount;
import com.domain.enums.ConnectionStatus;
import com.domain.repositories.EmailAccountRepository;
import com.infrastructure.email.service.EmailSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpenseSyncJob {
    private final EmailAccountRepository emailAccountRepository;
    private final EmailSyncService emailSyncService;

    // Threshold in seconds: accounts synced within this time are skipped
    @Value("${snapbill.sync.threshold-seconds:600}")
    private long syncThresholdSeconds;

    // Cron expression from properties
    @Value("${snapbill.sync.cron}")
    private String cronExpression;

    @Scheduled(cron = "${snapbill.sync.cron}")
    public void run() {
        log.info("Starting ExpenseSyncJob at {}", Instant.now());

        try {

            Instant threshold = Instant.now().minusSeconds(syncThresholdSeconds);

            // only account that haven't sync recently
            List<EmailAccount> accounts = emailAccountRepository.findAccountsToSync(ConnectionStatus.ACTIVE, threshold);

            if(accounts.isEmpty()) {
                log.error("No active accounts need syncing right now.");
                return;
            }

            for(EmailAccount account : accounts) {
                account.setLastSyncAt(Instant.now());
                Thread.ofVirtual().start(() -> syncAccountSafe(account));
            }
        }catch (Exception e) {
            log.error("ExpenseSyncJob failed: {}", e.getMessage(), e);
        }

    }

    private void syncAccountSafe(EmailAccount account) {
        try {
            log.info("Syncing account: {}", account.getProviderEmail());
            int syncedCount = emailSyncService.syncAccount(account);
            log.info("Account {} synced {} new messages", account.getProviderEmail(), syncedCount);
        } catch (Exception e) {
            log.error("Failed to sync account {}: {}", account.getProviderEmail(), e.getMessage(), e);
        }
    }

}
