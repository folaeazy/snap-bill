package com.expenseapp.app.interfaces;

import com.expenseapp.app.dto.accounts.ConnectedAccountsResponse;

import java.util.UUID;

public interface AccountService {
    ConnectedAccountsResponse getAccounts();
    void disconnectAccount(UUID id);
}
