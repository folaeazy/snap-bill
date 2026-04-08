package com.domain.events;

import java.util.UUID;

public record EmailProcessingRequested(UUID accountId) {
}
