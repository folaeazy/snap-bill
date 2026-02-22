package utils;

import com.domain.entities.Transaction;
import com.domain.utils.ExpenseFilters;

import com.domain.valueObjects.CurrencyCode;
import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
import org.junit.jupiter.api.Test;
import com.domain.valueObjects.Money;
import com.domain.valueObjects.TransactionDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ExpenseFiltersTest {

    @Test
    void isDebit_shouldFilterCorrectly() {
        Transaction debit = createDebit();
        Transaction credit = createCredit();

        assertThat(ExpenseFilters.isDebit().test(debit)).isTrue();
        assertThat(ExpenseFilters.isDebit().test(credit)).isFalse();

    }

    @Test
    void inMonth_shouldMatchCorrectly() {
        Transaction txJan = createWithDate(LocalDate.of(2026, 1, 15));
        Transaction txFeb = createWithDate(LocalDate.of(2026, 2, 10));

        assertThat(ExpenseFilters.inMonth(2026,1).test(txJan)).isTrue();
        assertThat(ExpenseFilters.inMonth(2026,1).test(txFeb)).isFalse();
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
