package com.infrastructure.email.listeners;

import com.domain.entities.EmailAccount;
import com.domain.entities.PipelineFailure;
import com.domain.events.EmailSyncCompleted;
import com.domain.events.EmailSyncRequested;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.PipelineFailureRepository;
import com.infrastructure.email.service.EmailSyncService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component

public class EmailSyncListener {

    private static final Log log = LogFactory.getLog(EmailSyncListener.class);
    private final EmailSyncService emailSyncService;
    private final ApplicationEventPublisher publisher;
    private final EmailAccountRepository emailAccountRepository;
    private final ExecutorService pipelineExecutor;
    private final ScheduledExecutorService retryExecutor;
    private final PipelineFailureRepository pipelineFailureRepository;

    public EmailSyncListener(EmailSyncService emailSyncService,
                             ApplicationEventPublisher publisher,
                             EmailAccountRepository emailAccountRepository,
                             @Qualifier("pipelineExecutor")ExecutorService pipelineExecutor,
                             @Qualifier("scheduledExecutor") ScheduledExecutorService retryExecutor,
                             PipelineFailureRepository pipelineFailureRepository) {
        this.emailSyncService = emailSyncService;
        this.publisher = publisher;
        this.emailAccountRepository = emailAccountRepository;
        this.pipelineExecutor = pipelineExecutor;
        this.retryExecutor = retryExecutor;
        this.pipelineFailureRepository = pipelineFailureRepository;
    }


    @EventListener
    public void handle(EmailSyncRequested event) {
        pipelineExecutor.execute(() -> {
            try {
                log.info("Email sync request event publish received ......");
                log.info("Starting now...............................");
               EmailAccount account = emailAccountRepository.findById(event.accountId())
                       .orElseThrow();

               int newEmails = emailSyncService.syncAccount(account);

                //publish to email sync complete
               publisher.publishEvent(new EmailSyncCompleted(account.getId(), newEmails));

            }catch (Exception e) {
                log.error("Sync failed for  account {}", e);

                // persist failure
                pipelineFailureRepository.save(
                        PipelineFailure.builder()
                                .accountId(event.accountId())
                                .pipelineStage("PROCESSING")
                                .message(e.getMessage())
                                .time(Instant.now())
                                .build()
                );

                // retry after delay
                retryExecutor.schedule(() -> {
                    pipelineExecutor.execute(() -> {
                        publisher.publishEvent(
                                new EmailSyncRequested(event.accountId())
                        );
                    });
                }, 30, TimeUnit.SECONDS);

            }
        });

    }
}
