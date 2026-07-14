package com.expenseapp.app.service;

import com.domain.entities.EmailAccount;
import com.domain.entities.User;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.SyncStatus;
import com.domain.events.EmailSyncRequested;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.UserRepository;
import com.expenseapp.app.dto.accounts.ConnectedAccountsResponse;
import com.expenseapp.app.dto.report.models.SyncTriggerResponse;
import com.expenseapp.app.interfaces.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final ApplicationEventPublisher publisher;


    /**
     * @return
     */
    @Override
    public ConnectedAccountsResponse getAccounts() {
        return null;
    }

    /**
     *
     */
    @Override
    public void disconnectAccount(UUID id) {

    }

    /**
     * @param user
     */
    @Override
    public SyncTriggerResponse triggerManualSync(User user) {
        List<EmailAccount> accountList = emailAccountRepository.findByUserAndStatus(user, ConnectionStatus.ACTIVE);
         if (accountList.isEmpty()) {
             return new SyncTriggerResponse(0, 0, "No active accounts to sync");
         }

        int triggered = 0;
        int skipped = 0;

        for (EmailAccount account : accountList) {
            if (account.getSyncStatus() == SyncStatus.SYNCING) {
                skipped++;
                continue; // already mid-sync, don't re-trigger — courtesy check, real guard is tryClaim()
            }
            publisher.publishEvent(new EmailSyncRequested(account.getId()));
            triggered++;
        }

        return new SyncTriggerResponse(triggered, skipped, "Sync triggered");

    }
}
