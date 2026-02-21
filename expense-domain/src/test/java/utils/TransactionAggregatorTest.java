package utils;

import com.domain.entities.Transaction;
import com.domain.utils.TransactionAggregator;
import com.domain.enums.CurrencyCode;
import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
import org.junit.jupiter.api.Test;
import com.domain.valueObjects.Category;
import com.domain.valueObjects.Money;
import com.domain.valueObjects.TransactionDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionAggregatorTest {

    @Test
    //@DisplayName("")
    void totalDebit_shouldSumOnlyDebits(){
        var txs = List.of(
                createDebit(1500,"subs", CurrencyCode.NGN),
                createDebit(1000,"subs", CurrencyCode.USD),
                createDebit(1500,"subs", CurrencyCode.NGN),
                createCrebit(1500,"subs", CurrencyCode.NGN)

        );

        assertThatThrownBy(()-> TransactionAggregator.totalDebits(txs)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transactions contain multiple currencies");

    }

    @Test
    void sumByCategory_shouldGroupCorrectly() {
        var txs = List.of(
                createDebit(4000, "Subscriptions", CurrencyCode.NGN),
                createDebit(2500, "Food", CurrencyCode.NGN),
                createDebit(1500, "Subscriptions", CurrencyCode.NGN)


        );

        Map<Category, Money> sums = TransactionAggregator.sumByCategory(txs);

        assertThat(sums).hasSize(2);
        assertThat(sums.get(Category.of("Subscriptions")).getAmount()).isEqualByComparingTo("5500.00");
        assertThat(sums.get(Category.of("Food")).getAmount()).isEqualByComparingTo("2500.00");
    }

    @Test
    void totalDebits_shouldThrowOnMixedCurrencies() {
        var txs = List.of(
                createDebit(100,"subs", CurrencyCode.USD),
                createDebit(5000,"suns", CurrencyCode.NGN)
        );

        assertThatThrownBy(() -> TransactionAggregator.totalDebits(txs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multiple currencies");
    }



    // Helper
    private Transaction createDebit(long amount, String categoryName, CurrencyCode currency) {
        return Transaction.create(
                TransactionType.DEBIT,
                Money.of(BigDecimal.valueOf(amount), currency),
                TransactionDate.now(),
                null,
                Category.of(categoryName),
                Set.of(),
                null,
                null,
                TransactionSource.MANUAL,
                null
        );
    }

    // Helper
    private Transaction createCrebit(long amount, String categoryName, CurrencyCode currency) {
        return Transaction.create(
                TransactionType.CREDIT,
                Money.of(BigDecimal.valueOf(amount), currency),
                TransactionDate.now(),
                null,
                Category.of(categoryName),
                Set.of(),
                null,
                null,
                TransactionSource.MANUAL,
                null
        );
    }
}
