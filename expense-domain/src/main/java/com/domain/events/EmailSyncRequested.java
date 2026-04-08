package com.domain.events;

import java.util.UUID;

public record EmailSyncRequested(UUID accountId) {
}
