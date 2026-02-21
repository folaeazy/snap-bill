package com.domain.exceptions;


import com.domain.enums.CurrencyCode;

/**
 * Thrown when currencies don't match in a context where they must be the same.
 * Examples:
 * - Transaction amount currency ≠ account default currency
 * - Adding/subtracting Money objects with different currencies
 */
public class InconsistentCurrencyException extends DomainValidationException{

    public InconsistentCurrencyException(String message) {
        super(message);
    }

    public InconsistentCurrencyException(String message, Throwable cause) {
        super(message, cause);
    }

    // Example convenience factory
    public static InconsistentCurrencyException mismatch(
            CurrencyCode transactionCurrency,
            CurrencyCode accountCurrency) {

        return new InconsistentCurrencyException(
                "Transaction currency (" + transactionCurrency.getCode() +
                        ") does not match account currency (" + accountCurrency.getCode() + ")"
        );
    }
}
