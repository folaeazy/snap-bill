package com.domain.events;

import com.domain.entities.EmailAccount;

import java.util.UUID;

public record EmailSyncCompleted(EmailAccount account, int newEmailCount) {
}
