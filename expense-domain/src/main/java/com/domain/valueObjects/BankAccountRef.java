package com.domain.valueObjects;


import com.domain.enums.CurrencyCode;

import java.util.Objects;

/**
 * Lightweight immutable reference to a bank account.
 * Used when we don't need the full BankAccount aggregate yet.
 */
public final class BankAccountRef {

    private final String accountId;       // internal ID or external reference
    private final String label;           // e.g. "GTBank Savings", "Main Checking"
    private final String last4;           // last 4 digits for display (masked)
    private final CurrencyCode currency;  // default currency of the account

    public BankAccountRef(String accountId, String label, String last4, CurrencyCode currency) {
        this.accountId = Objects.requireNonNull(accountId, "Account ID is required").trim();
        this.label = Objects.requireNonNull(label, "Label is required").trim();
        this.last4 = last4 != null ? last4.trim() : null;
        this.currency = Objects.requireNonNull(currency, "Currency is required");
    }

    public static BankAccountRef of(String accountId, String label, CurrencyCode currency) {
        return new BankAccountRef(accountId, label, null, currency);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getLabel() {
        return label;
    }

    public String getLast4() {
        return last4;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccountRef that = (BankAccountRef) o;
        return accountId.equals(that.accountId);
    }

    @Override
    public int hashCode() {
        return accountId.hashCode();
    }

    @Override
    public String toString() {
        return label + " (****" + (last4 != null ? last4 : "xxxx") + ")";
    }
}
