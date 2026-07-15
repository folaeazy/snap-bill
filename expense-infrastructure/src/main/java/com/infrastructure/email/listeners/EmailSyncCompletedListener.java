package com.infrastructure.email.listeners;


import com.domain.enums.ProcessingStatus;
import com.domain.events.EmailProcessingRequested;
import com.domain.events.EmailSyncCompleted;
import com.domain.repositories.RawEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class EmailSyncCompletedListener {
    private final ApplicationEventPublisher publisher;
    private final ExecutorService pipelineExecutor;
    private final RawEmailRepository rawEmailRepository;

    public EmailSyncCompletedListener(ApplicationEventPublisher publisher,
                                      @Qualifier("pipelineExecutor")ExecutorService pipelineExecutor,
                                      RawEmailRepository rawEmailRepository
                                      ) {
        this.publisher = publisher;
        this.pipelineExecutor = pipelineExecutor;
        this.rawEmailRepository = rawEmailRepository;
    }

    /**
     * Observation :
     * For previously synced email - processing status remains PENDING and won't reach LLM extraction stage
     *
     */

    @EventListener
    public void handle(EmailSyncCompleted event) {
        log.info("Received email sync completed, checking to proceed....");

        boolean hasPendingWork =  rawEmailRepository.existsByAccountIdAndStatus(event.account(), ProcessingStatus.PENDING);
        if(hasPendingWork) {
            log.info("proceeding to LLM processing ........");
            pipelineExecutor.execute(()-> {
                try {
                    publisher.publishEvent(new EmailProcessingRequested(event.account().getId()));
                }catch (Exception e) {
                    log.error("Publishing completed failed", e);
                }

            });

        }

    }
}
