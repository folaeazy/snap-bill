package com.expenseapp.app.interfaces;

import com.domain.entities.User;
import com.expenseapp.app.dto.accounts.ConnectedAccountsResponse;
import com.expenseapp.app.dto.report.models.SyncTriggerResponse;

import java.util.UUID;

public interface AccountService {
    ConnectedAccountsResponse getAccounts();
    void disconnectAccount(UUID id);

    SyncTriggerResponse triggerManualSync(User user);
}
