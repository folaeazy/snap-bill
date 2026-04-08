package com.domain.events;

import java.util.UUID;

public record EmailSyncCompleted(UUID accountId, int newEmailCount) {
}
