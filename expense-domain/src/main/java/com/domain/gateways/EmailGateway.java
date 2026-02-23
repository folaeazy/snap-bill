package com.domain.gateways;


import com.domain.entities.EmailAccount;
import com.domain.exceptions.EmailGatewayException;
import com.domain.model.EmailMessage;

import java.time.Instant;
import java.util.List;

/**
 * Gateway interface (port) for interacting with external email providers.
 *
 * This is the contract that domain services use to fetch emails without knowing
 * whether it's Gmail, Outlook, or any future provider.
 *
 * Implementations go in expense-infrastructure (e.g. GmailEmailGateway, OutlookEmailGateway).
 */
public interface EmailGateway {


    /**
     * Fetch new or unread emails since the last successful sync time.
     *
     * @param account The connected email account with provider details and tokens
     * @param since   The timestamp of the last successful sync
     * @return List of raw email messages ready for processing/parsing
     * @throws EmailGatewayException if authentication fails, rate limit exceeded, network error, etc.
     */
    List<EmailMessage> fetchNewMessages(EmailAccount account, Instant since);

    /**
     * Optional: Mark a batch of emails as processed (e.g. mark as read, archive, add label).
     *
     * This is useful if you want to prevent re-processing the same emails.
     * Some implementations may do nothing (e.g. if you want to keep inbox untouched).
     *
     * @param account  The email account
     * @param messages The messages that were successfully processed
     */
    default void markMessagesAsProcessed(EmailAccount account, List<EmailMessage> messages) {
        // Default no-op implementation — override in concrete gateways if needed
    }

    /**
     * Check if the access token is still valid / not revoked.
     *
     * Useful before attempting a sync to avoid unnecessary failures.
     */
    boolean isConnectionValid(EmailAccount account);
}
