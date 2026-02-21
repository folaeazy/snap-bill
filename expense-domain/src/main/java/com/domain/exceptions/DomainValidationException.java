package com.domain.exceptions;


/**
 * Base exception for all business/domain rule violations in the expense-core domain.

 * Use this as the root for any checked or unchecked exceptions that represent
 * invalid business state or invariant violations.

 * Subclasses should provide clear, user-friendly messages that can be shown
 * in the UI or API responses (after proper handling in the application layer).
 */
public class DomainValidationException extends  RuntimeException {
    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }



}
