package entities;

import com.domain.entities.Transaction;
import com.domain.valueObjects.CurrencyCode;
import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
import com.domain.exceptions.TransactionValidationException;
import com.domain.valueObjects.*;
import com.expensecore.valueObjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TransactionTest {

    private TransactionId id;
    private Money amountNgn;
    private TransactionDate today;

    @BeforeEach
    void setUp() {
        id = TransactionId.generate();
        amountNgn = Money.of(new BigDecimal("1500.0556"), CurrencyCode.NGN);
        today = TransactionDate.now();

    }

    @Test
    @DisplayName("should create valid debit transaction using factory")
    void shouldCreateValidDebit() {
        Transaction tx = Transaction.create(
                TransactionType.DEBIT,
                amountNgn,
                today,
                Merchant.of("Spotify"),
                Category.of("Subscription"),
                Set.of(Tag.of("Recurring")),
                BankAccountRef.of("acc-123", "Main Account", CurrencyCode.NGN),
                Description.of("Monthly subscription"),
                TransactionSource.MANUAL,
                null

        );

        assertThat(tx.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(tx.getAmount()).isEqualTo(amountNgn);
        assertThat(tx.isDebit()).isTrue();
        assertThat(tx.getCreatedAt()).isNotNull();
        assertThat(tx.getUpdatedAt()).isEqualTo(tx.getCreatedAt());

    }


    @Test
    @DisplayName("should reject future debit date (basic rule)")
    void shouldRejectFutureDebitDate() {
        TransactionDate future = TransactionDate.of(LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> Transaction.create(
                TransactionType.DEBIT, amountNgn, future, null, null, Set.of(), null, null,
                TransactionSource.MANUAL, null)).isInstanceOf(TransactionValidationException.class).hasMessageContaining("Debit transactions cannot have future dates");

    }

    @Test
    @DisplayName("with-methods should create new instance and update timestamp")
    void withMethodsShouldBeImmutable() {

        Transaction original = Transaction.create(
                TransactionType.DEBIT, amountNgn, today, null, null, Set.of(), null, null,
                TransactionSource.MANUAL, null);

        Transaction updated = original.withCategory(Category.of("Entertainment"));

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.getCategory().getName()).isEqualTo("Entertainment");
        assertThat(updated.getUpdatedAt()).isAfter(original.getUpdatedAt());
    }


}
