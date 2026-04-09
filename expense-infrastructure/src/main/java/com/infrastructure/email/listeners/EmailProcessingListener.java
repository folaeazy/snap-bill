package com.infrastructure.email.listeners;

import com.domain.events.EmailProcessingRequested;
import com.infrastructure.email.service.EmailProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProcessingListener {
    private final ExecutorService pipelineExecutor;
    private final EmailProcessingService emailProcessingService;


    @EventListener
    public void handle(EmailProcessingRequested event) {
        pipelineExecutor.execute(() -> {
            try {

                log.info("Received EmailSyncCompleted for account {}. Starting LLM processing...", event.accountId());
                emailProcessingService.processPendingEmails();
                log.info("LLM processing completed for account {}", event.accountId());
            }catch (Exception e) {
                log.error("LLM processing failed for account {}", event.accountId(), e);
            }

        });

    }
}
