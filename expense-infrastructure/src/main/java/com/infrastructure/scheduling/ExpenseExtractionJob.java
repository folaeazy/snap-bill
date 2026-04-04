package com.infrastructure.scheduling;

import com.domain.entities.EmailAccount;
import com.domain.entities.RawEmailMessage;
import com.domain.domain.Transaction;
import com.domain.entities.TransactionEntity;
import com.domain.entities.User;
import com.domain.enums.ProcessingStatus;
import com.domain.repositories.RawEmailRepository;
import com.domain.repositories.TransactionRepository;
import com.infrastructure.email.service.ExpenseExtractionService;
import com.infrastructure.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseExtractionJob {

    private final RawEmailRepository rawEmailRepository;
    private final ExpenseExtractionService expenseExtractionService;
    private final TransactionRepository transactionRepository;
    private final EntityMapper entityMapper;

    private static final int MAX_CONCURRENT_TASK = 10;  // limit cause of API rate limit

    // Scheduler Entry point

    @Scheduled(fixedRate = 2 * 60 * 1000) //2 minute for testing
    public void runScheduled() {
        log.info("Started scheduled extraction");
        processPendingEmails();
    }

    //REUSABLE FOR SCHEDULER AND ON LOGIN/SIGNUP
    @Transactional
    public void processPendingEmails() {
        List<RawEmailMessage> emails = rawEmailRepository.findTop50ByProcessedOrderByReceivedDateAsc(ProcessingStatus.PENDING);

        if(emails.isEmpty()) {
            log.info("No pending email to process");
            return;
        }
        log.info("processing email of size {}", emails.size());
        processBatch(emails);
    }


    // CONCURRENT BATCH PROCESSING
    private void processBatch(List<RawEmailMessage> emails) {
        Semaphore semaphore = new Semaphore(MAX_CONCURRENT_TASK);
        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<? extends Future<?>> futures = emails.stream()
                    .map(email -> executor.submit(() -> {
                        try{
                            semaphore.acquire();
                            processEmail(email);
                            System.out.println(Thread.currentThread());
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                        }finally {
                            semaphore.release();
                        }
                    }))
                    .toList();

            // wait for all tasks
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("Task execution failed", e);
                }
            }

        }
    }



    //CORe Logic

    private void processEmail(RawEmailMessage email) {
        try{
            email.setProcessed(ProcessingStatus.PROCESSING);
            rawEmailRepository.save(email);

            Optional<Transaction> txOpt =
                    expenseExtractionService.extract(email);
            // get user and email account
            EmailAccount emailAccount = email.getEmailAccount();
            User user = emailAccount.getUser();


            if (txOpt.isPresent()) {
                log.info("Optionally logging after extracting email from AI........");
                // Map to transaction Entity
                TransactionEntity entity = entityMapper.toEntity(user, emailAccount, txOpt.get());
                transactionRepository.save(entity);

                email.setProcessed(ProcessingStatus.PROCESSED);
                email.setFailureReason(null);

            } else {
                email.setProcessed(ProcessingStatus.FAILED);
                email.setFailureReason("Validation failed or not a transaction");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        rawEmailRepository.save(email);
    }



}
