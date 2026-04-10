package com.infrastructure.email.listeners;

import com.domain.entities.PipelineFailure;
import com.domain.events.EmailProcessingRequested;
import com.domain.events.EmailSyncRequested;
import com.domain.repositories.PipelineFailureRepository;
import com.infrastructure.email.service.EmailProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class EmailProcessingListener {
    private final ExecutorService pipelineExecutor;
    private final EmailProcessingService emailProcessingService;
    private final PipelineFailureRepository pipelineFailureRepository;
    private final ScheduledExecutorService retryExecutor;

    public EmailProcessingListener(@Qualifier("pipelineExecutor")ExecutorService pipelineExecutor,
                                   EmailProcessingService emailProcessingService,
                                   PipelineFailureRepository pipelineFailureRepository,
                                   @Qualifier("scheduledExecutor")ScheduledExecutorService retryExecutor) {
        this.pipelineExecutor = pipelineExecutor;
        this.emailProcessingService = emailProcessingService;
        this.pipelineFailureRepository = pipelineFailureRepository;
        this.retryExecutor = retryExecutor;
    }


    @EventListener
    public void handle(EmailProcessingRequested event) {
        pipelineExecutor.execute(() -> {
            try {

                log.info("Received EmailSyncCompleted for account {}. Starting LLM processing...", event.accountId());
                emailProcessingService.processPendingEmailsForAccounts(event.accountId());
                log.info("LLM processing completed for account {}", event.accountId());
            }catch (Exception e) {
                log.error("LLM processing failed for account {}", event.accountId(), e);

                pipelineFailureRepository.save(
                        PipelineFailure.builder()
                                .accountId(event.accountId())
                                .pipelineStage("PROCESSING")
                                .message(e.getMessage())
                                .time(Instant.now())
                                .build()

                );
                // retry after delay
//                retryExecutor.schedule(() -> {
//                   // TODO
//
//                    );
//                }, 30, TimeUnit.SECONDS);
            }

        });

    }
}
