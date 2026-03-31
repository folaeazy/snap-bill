package com.expenseapp.app.service;

import com.expenseapp.app.dto.accounts.ConnectedAccountsResponse;
import com.expenseapp.app.interfaces.AccountService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {
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
}
