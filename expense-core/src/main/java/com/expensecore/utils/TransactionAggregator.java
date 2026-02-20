package com.expensecore.utils;



import com.expensecore.entities.Transaction;
import com.expensecore.enums.CurrencyCode;
import com.expensecore.valueObjects.Category;
import com.expensecore.valueObjects.Money;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure aggregation / summary functions over collections of transactions.
 */
public final class TransactionAggregator {

    private TransactionAggregator() {
        // prevent instantiation
    }

    /**
     * Total amount of debits (expenses) in the given collection.
     */
    public static Money totalDebits(Collection<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return Money.zero(CurrencyCode.NGN); // or throw / require non-empty
        }

        CurrencyCode currency = determineCommonCurrency(transactions);
        BigDecimal sum = transactions.stream()
                .filter(TransactionFilters.isDebit())
                .map(Transaction::getAmount)
                .map(Money::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Money.of(sum, currency);
    }

    /**
     * Group debits by category and sum amounts.
     */

    public static Map<Category, Money> sumByCategory(Collection<Transaction> transactions) {
        CurrencyCode currency = determineCommonCurrency(transactions);

        return transactions.stream()
                .filter(TransactionFilters.isDebit())
                .filter(t->t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                Money.zero(currency),
                                Transaction::getAmount,
                                Money::add

                        )
                ));

    }

    private static CurrencyCode determineCommonCurrency(Collection<Transaction> txs) {
        Set<CurrencyCode> currencies = txs.stream()
                .map(t -> t.getAmount().getCurrency())
                .collect(Collectors.toSet());

        if (currencies.size() != 1) {
            throw new IllegalArgumentException("Transactions contain multiple currencies");
        }
        return currencies.iterator().next();
    }
}
