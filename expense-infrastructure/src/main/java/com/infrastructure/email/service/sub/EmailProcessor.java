package com.infrastructure.email.service.sub;

import com.domain.entities.RawEmailMessage;
import com.domain.enums.ProcessingStatus;
import com.domain.exceptions.DomainValidationException;
import com.domain.repositories.RawEmailRepository;
import com.domain.repositories.TransactionRepository;
import com.infrastructure.email.service.ExpenseExtractionService;
import com.infrastructure.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProcessor {

    private final ExpenseExtractionService expenseExtractionService;
    private final TransactionRepository transactionRepository;
    private final EntityMapper entityMapper;
    private final RawEmailRepository rawEmailRepository;


    @Transactional
    public void processSingleEmail(RawEmailMessage email) {

        //TODO : claiming
        try {
            email.setProcessed(ProcessingStatus.PROCESSING);
            rawEmailRepository.save(email);

            var txOpt = expenseExtractionService.extract(email);

            if (txOpt.isPresent()) {
                var entity = entityMapper.toEntity(
                        email.getEmailAccount().getUser(),
                        email.getEmailAccount(),
                        email.getProviderMessageId(),
                        txOpt.get()
                );
                transactionRepository.save(entity);

                email.setProcessed(ProcessingStatus.PROCESSED);
                email.setFailureReason(null);
            } else {
                markFailed(email,"VALIDATION_FAILED");
            }
        } catch (DomainValidationException e) {

            markFailed(email, "DOMAIN: " + e.getMessage(), false);

        } catch (DataIntegrityViolationException e) {

            //  DB-level idempotency fallback
            log.warn("Duplicate prevented for {}", email.getId());

            email.setProcessed(ProcessingStatus.PROCESSED);

        } catch (Exception e) {

            log.error("System error for {}", email.getId(), e);

            markFailed(email, "SYSTEM_ERROR", true);

        } finally {
            rawEmailRepository.save(email);
        }
    }



    private void markFailed(RawEmailMessage email, String reason) {
        markFailed(email, reason, false);
    }

    private void markFailed(RawEmailMessage email, String reason, boolean retryable) {

        email.setFailureReason(reason);

        if (retryable) {
            int retry = email.getRetryCount() + 1;

            email.setRetryCount(retry);

            if (retry >= 3) {
                email.setProcessed(ProcessingStatus.FAILED);
            } else {
                email.setProcessed(ProcessingStatus.PENDING);

                // exponential backoff
                email.setNextRetryAt(Instant.now().plusSeconds((long) Math.pow(2, retry) * 60));
            }

        } else {
            email.setProcessed(ProcessingStatus.FAILED);
        }
    }
}
