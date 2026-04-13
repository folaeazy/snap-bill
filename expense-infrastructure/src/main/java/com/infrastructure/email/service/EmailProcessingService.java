package com.infrastructure.email.service;

import com.domain.entities.RawEmailMessage;
import com.domain.repositories.RawEmailRepository;
import com.infrastructure.email.service.sub.EmailProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@Slf4j
public class EmailProcessingService {

    private final RawEmailRepository rawEmailRepository;
    private final EmailProcessor emailprocessor;

    private final ExecutorService pipelineExecutor;

    private static final int MAX_CONCURRENT_PER_ACCOUNT = 8;// configurable later
    private static final int BATCH_SIZE = 25;

    public EmailProcessingService(RawEmailRepository rawEmailRepository,
                                  EmailProcessor emailprocessor,
                                  @Qualifier("pipelineExecutor") ExecutorService pipelineExecutor) {
        this.rawEmailRepository = rawEmailRepository;
        this.emailprocessor = emailprocessor;
        this.pipelineExecutor = pipelineExecutor;
    }

    /**
     * Method called by listener
     */
    public void processPendingEmailsForAccounts(UUID accountId) {
        Instant now = Instant.now();
        Instant timeout = now.minusSeconds(600); // 10 minutes
        int maxRetry = 3;

        UUID token = UUID.randomUUID();

        //Get Ids
        List<UUID> ids = rawEmailRepository.findIdsForClaim(accountId,timeout,now,maxRetry,BATCH_SIZE);


        if (ids.isEmpty()) {
            log.info("No pending emails to process for account {}", accountId);
            return;
        }

        // Claim emails in batch with batch token
        int claimed = rawEmailRepository.claimByIds(ids,now ,token);
        if(claimed == 0) {
            log.info("Failed to claim emails to process for account {}", accountId);
            return;
        }

        // Fetch exact row claimed
        List<RawEmailMessage> emails = rawEmailRepository.findByClaimToken(token);
        log.info("Processing  pending emails of size {} for account {}", emails.size(), accountId);
        processBatch(emails);
    }

    private void processBatch(List<RawEmailMessage> emails) {
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_PER_ACCOUNT);


            List<CompletableFuture<Void>> futures = emails.stream()
                    .map(email -> CompletableFuture.runAsync(() -> {
                        try {
                            semaphore.acquire();
                            emailprocessor.processSingleEmail(email);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            semaphore.release();
                        }
                    }, pipelineExecutor))
                    .toList();

            // Wait for all tasks
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    }


}
