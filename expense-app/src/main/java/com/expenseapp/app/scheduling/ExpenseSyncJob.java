package com.expenseapp.app.scheduling;


import com.domain.entities.EmailAccount;
import com.domain.enums.ConnectionStatus;
import com.domain.events.EmailSyncRequested;
import com.domain.repositories.EmailAccountRepository;
import com.infrastructure.email.service.EmailSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

@Component
@Slf4j

public class ExpenseSyncJob {
    private final EmailAccountRepository emailAccountRepository;
    private final ApplicationEventPublisher publisher;
    private final ExecutorService pipelineExecutor;
    private static final int ACCOUNT_LIMIT = 50;

    private final Semaphore accountSemaphore = new Semaphore(ACCOUNT_LIMIT);


    // Threshold in seconds: accounts synced within this time are skipped
    @Value("${snapbill.sync.threshold-seconds:600}")
    private long syncThresholdSeconds;

    // Cron expression from properties
    @Value("${snapbill.sync.cron}")
    private String cronExpression;

    public ExpenseSyncJob(EmailAccountRepository emailAccountRepository,
                          ApplicationEventPublisher publisher,
                          @Qualifier("pipelineExecutor")ExecutorService pipelineExecutor) {
        this.emailAccountRepository = emailAccountRepository;
        this.publisher = publisher;
        this.pipelineExecutor = pipelineExecutor;
    }

    /**
     * Scheduler that triggers to whole processing pipeline.
     */
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
                // Update lastSync not to trigger again
                account.setLastSyncAt(Instant.now());
                emailAccountRepository.save(account);


                // Publish event to listener to start the pipeline flow
                // Async fire-and-forget publishing
                pipelineExecutor.execute(() -> {
                    try {
                        accountSemaphore.acquire();
                        publisher.publishEvent(new EmailSyncRequested(account.getId()));
                    } catch (Exception e) {
                        log.error("Failed to publish EmailSyncRequested for account {}", account.getId(), e);
                    }finally {
                        accountSemaphore.release();
                    }
                });

            }
        }catch (Exception e) {
            log.error("ExpenseSyncJob failed: {}", e.getMessage(), e);
        }
    }

}
