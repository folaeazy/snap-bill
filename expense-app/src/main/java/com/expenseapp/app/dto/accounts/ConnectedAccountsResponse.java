package com.expenseapp.app.dto.accounts;

import java.util.List;

public record ConnectedAccountsResponse(
        List<ConnectedAccountResponse> accounts,
        int total
) {
}
