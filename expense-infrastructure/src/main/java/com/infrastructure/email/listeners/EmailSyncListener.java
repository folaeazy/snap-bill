package com.infrastructure.email.listeners;

import com.domain.entities.EmailAccount;
import com.domain.events.EmailSyncCompleted;
import com.domain.events.EmailSyncRequested;
import com.domain.repositories.EmailAccountRepository;
import com.infrastructure.email.service.EmailSyncService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class EmailSyncListener {

    private static final Log log = LogFactory.getLog(EmailSyncListener.class);
    private final EmailSyncService emailSyncService;
    private final ApplicationEventPublisher publisher;
    private final EmailAccountRepository emailAccountRepository;
    private final ExecutorService pipelineExecutor;


    @EventListener
    public void handle(EmailSyncRequested event) {
        pipelineExecutor.submit(() -> {
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
            }
        });

    }
}
