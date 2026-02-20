package com.expensecore.exceptions;


/**
 * Thrown when a required field is missing or null in a context where it's mandatory.
 *
 * Used sparingly — many fields are optional by design (merchant, category, tags...).
 * Use this mainly for truly invariant-required fields (type, amount, date, source).
 */
public class MissingRequiredFieldException extends DomainValidationException{
    public MissingRequiredFieldException(String fieldName) {
        super("Required field is missing or null: " + fieldName);
    }

    public MissingRequiredFieldException(String fieldName, String additionalMessage) {
        super("Required field is missing or null: " + fieldName + ". " + additionalMessage);
    }
}
