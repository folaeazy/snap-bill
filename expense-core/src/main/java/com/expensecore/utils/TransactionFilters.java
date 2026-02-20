package com.expensecore.utils;



import com.expensecore.entities.Transaction;
import com.expensecore.enums.TransactionType;
import com.expensecore.valueObjects.Category;
import com.expensecore.valueObjects.Description;
import com.expensecore.valueObjects.Merchant;
import com.expensecore.valueObjects.Tag;

import java.time.LocalDate;
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
        return t-> t.getType() == TransactionType.DEBIT;
    }

    public static Predicate<Transaction> isCrebit () {
        return t-> t.getType() == TransactionType.CREDIT;
    }

    public static Predicate<Transaction> inDateRange(LocalDate  start, LocalDate endInclusive) {
        return t -> {
            LocalDate d = t.getDate().getDate();
            return !d.isBefore(start) && !d.isAfter(endInclusive);
        };
    }

    public static Predicate<Transaction> inMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return inDateRange(start, end);
    }

    public static Predicate<Transaction> byCategory(Category category) {
        return t -> category.equals(t.getCategory());
    }

    public static Predicate<Transaction> byMerchant(Merchant merchant) {
        return t -> merchant != null && merchant.equals(t.getMerchant());
    }

    public static Predicate<Transaction> byTag(Tag tag) {
        return t -> t.getTags().contains(tag);
    }

    public static Predicate<Transaction> hasDescriptionContaining(String substring) {
        return t -> {
            Description desc = t.getDescription();
            return desc != null && !desc.isEmpty() &&
                    desc.getText().toLowerCase().contains(substring.toLowerCase());
        };
    }
}
