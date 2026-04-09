package com.infrastructure.email.listeners;


import com.domain.events.EmailProcessingRequested;
import com.domain.events.EmailSyncCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSyncCompletedListener {
    private final ApplicationEventPublisher publisher;
    private final ExecutorService pipelineExecutor;

    @EventListener
    public void handle(EmailSyncCompleted event) {
        log.info("Received email sync completed, checking to proceed....");
        if(event.newEmailCount()> 0) {
            log.info("proceeding to syncing ........");
            pipelineExecutor.execute(()-> {
                try {
                    publisher.publishEvent(new EmailProcessingRequested(event.accountId()));
                }catch (Exception e) {
                    log.error("Publishing completed failed", e);
                }

            });

        }

    }
}
