package utils;


import entities.Transaction;

import java.util.function.Predicate;

/**
 * Pure functions / predicates for filtering collections of transactions.
 * Meant to be used with Stream API, e.g.:
 * transactions.stream()
 *     .filter(TransactionFilters.isDebit())
 *     .filter(TransactionFilters.inMonth(2026, 2))
 *     .toList();
 */
public final class TransactionFilters {

    private TransactionFilters() {} // prevent instantiation

    public static Predicate<Transaction> isDebit () {
        return t-> t.ge
    }
}
