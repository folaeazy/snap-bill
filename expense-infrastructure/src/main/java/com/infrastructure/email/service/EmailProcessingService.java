package com.infrastructure.email.service;

import com.domain.entities.RawEmailMessage;
import com.domain.enums.ProcessingStatus;
import com.domain.repositories.RawEmailRepository;
import com.infrastructure.email.service.sub.EmailProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessingService {

    private final RawEmailRepository rawEmailRepository;
    private final EmailProcessor emailprocessor;

    private final ExecutorService pipelineExecutor;

    private static final int MAX_CONCURRENT_PER_ACCOUNT = 8;// configurable later
    private static final int BATCH_SIZE = 25;

    /**
     * Method called by listener
     */
    public void processPendingEmailsForAccounts(UUID accountId) {
        List<RawEmailMessage> emails =
                rawEmailRepository.findTopByEmailAccountAndProcessedOrderByReceivedDateAsc(accountId,ProcessingStatus.PENDING, BATCH_SIZE);

        if (emails.isEmpty()) {
            log.info("No pending emails to process for account {}", accountId);
            return;
        }

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
