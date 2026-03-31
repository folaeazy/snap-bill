package com.expenseapp.app.dto.accounts;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ConnectedAccountResponse(

        UUID id,

        String email,

        String provider,     // GOOGLE, OUTLOOK

        String status,       // ACTIVE / DISCONNECTED

        Instant lastSyncedAt,

        String lastSyncedDisplay
) {
}
