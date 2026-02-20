package utils;

import com.expensecore.entities.Transaction;
import com.expensecore.utils.TransactionFilters;

import com.expensecore.enums.CurrencyCode;
import com.expensecore.enums.TransactionSource;
import com.expensecore.enums.TransactionType;
import org.junit.jupiter.api.Test;
import com.expensecore.valueObjects.Money;
import com.expensecore.valueObjects.TransactionDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransactionFiltersTest {

    @Test
    void isDebit_shouldFilterCorrectly() {
        Transaction debit = createDebit();
        Transaction credit = createCredit();

        assertThat(TransactionFilters.isDebit().test(debit)).isTrue();
        assertThat(TransactionFilters.isDebit().test(credit)).isFalse();

    }

    @Test
    void inMonth_shouldMatchCorrectly() {
        Transaction txJan = createWithDate(LocalDate.of(2026, 1, 15));
        Transaction txFeb = createWithDate(LocalDate.of(2026, 2, 10));

        assertThat(TransactionFilters.inMonth(2026,1).test(txJan)).isTrue();
        assertThat(TransactionFilters.inMonth(2026,1).test(txFeb)).isFalse();
    }





    // Helpers
    private Transaction createDebit() {
        return Transaction.create(TransactionType.DEBIT, Money.of(BigDecimal.TEN, CurrencyCode.USD),
                TransactionDate.now(), null, null, Set.of(), null, null, TransactionSource.MANUAL, null);
    }

    private Transaction createCredit() {
        return Transaction.create(TransactionType.CREDIT, Money.of(BigDecimal.TEN, CurrencyCode.USD),
                TransactionDate.now(), null, null, Set.of(), null, null, TransactionSource.MANUAL, null);
    }

    private Transaction createWithDate(LocalDate date) {
        return Transaction.create(TransactionType.DEBIT, Money.of(BigDecimal.ONE, CurrencyCode.NGN),
                TransactionDate.of(date), null, null, Set.of(), null, null, TransactionSource.MANUAL, null);
    }
}
