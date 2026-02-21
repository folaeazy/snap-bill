package com.domain.exceptions;

/**
 * Thrown when a monetary amount violates domain rules.
 * Examples:
 * - Amount is zero or negative for a debit/credit transaction
 * - Amount has invalid scale/precision for the currency
 */
public class InvalidAmountException extends DomainValidationException{

    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }

    // Convenience static factory (optional but clean)
    public static InvalidAmountException zeroOrNegative() {
        return new InvalidAmountException("Amount must be positive (greater than zero)");
    }

    public static InvalidAmountException invalidScale(int expectedScale) {
        return new InvalidAmountException("Amount must have exactly " + expectedScale + " decimal places for this currency");
    }
}
