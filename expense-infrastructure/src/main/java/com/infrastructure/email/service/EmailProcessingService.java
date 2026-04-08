package com.infrastructure.email.service;

import com.domain.entities.RawEmailMessage;
import com.domain.enums.ProcessingStatus;
import com.domain.repositories.RawEmailRepository;
import com.domain.repositories.TransactionRepository;
import com.infrastructure.mapper.EntityMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessingService {

    private final RawEmailRepository rawEmailRepository;
    private final ExpenseExtractionService expenseExtractionService;
    private final TransactionRepository transactionRepository;
    private final EntityMapper entityMapper;

    private static final int MAX_CONCURRENT_TASKS = 10;   // configurable later

    @Transactional
    public void processPendingEmails() {
        List<RawEmailMessage> emails = rawEmailRepository.findTop50ByProcessedOrderByReceivedDateAsc(ProcessingStatus.PENDING);

        if (emails.isEmpty()) {
            log.info("No pending emails to process");
            return;
        }

        log.info("Processing batch of {} pending emails", emails.size());
        processBatch(emails);
    }

    private void processBatch(List<RawEmailMessage> emails) {
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_TASKS);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<? extends Future<?>> futures = emails.stream()
                    .map(email -> executor.submit(() -> {
                        try {
                            semaphore.acquire();
                            processSingleEmail(email);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            semaphore.release();
                        }
                    }))
                    .toList();

            // Wait for all tasks
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Task failed", e);
                }
            }
        }
    }

    private void processSingleEmail(RawEmailMessage email) {
        try {
            email.setProcessed(ProcessingStatus.PROCESSING);
            rawEmailRepository.save(email);

            var txOpt = expenseExtractionService.extract(email);

            if (txOpt.isPresent()) {
                var entity = entityMapper.toEntity(
                        email.getEmailAccount().getUser(),
                        email.getEmailAccount(),
                        txOpt.get()
                );
                transactionRepository.save(entity);

                email.setProcessed(ProcessingStatus.PROCESSED);
                email.setFailureReason(null);
            } else {
                email.setProcessed(ProcessingStatus.FAILED);
                email.setFailureReason("Validation failed or not a transaction");
            }
        } catch (Exception e) {
            log.error("Failed to process email {}", email.getId(), e);
            email.setProcessed(ProcessingStatus.FAILED);
            email.setFailureReason(e.getMessage());
        } finally {
            rawEmailRepository.save(email);
        }
    }
}
