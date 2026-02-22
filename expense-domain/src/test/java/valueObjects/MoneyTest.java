package valueObjects;

import com.domain.valueObjects.CurrencyCode;
import com.domain.exceptions.InconsistentCurrencyException;
import com.domain.exceptions.InvalidAmountException;
import com.domain.valueObjects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

public class MoneyTest {


    @Test
    @DisplayName("should create valid money and preserve scale")
    void shouldCreateAndNormalizeScale() {
        Money money = Money.of(new BigDecimal("123.456"), CurrencyCode.GBP);
        assertThat(money.getAmount()).isEqualByComparingTo("123.456");
        assertThat(money.getCurrency()).isEqualTo(CurrencyCode.GBP);
    }

    @Test
    @DisplayName("should reject negative amount")
    void shouldRejectNegativeAmount() {
            assertThatThrownBy(()->
                    Money.of(new BigDecimal("-100.00"), CurrencyCode.GBP))
                    .isInstanceOf(InvalidAmountException.class)
                    .hasMessageContaining("amount cannot be negative");
    }

    @Test
    @DisplayName("should add two money object")
    void shouldAddSameCurrency() {
        Money a = Money.of(new BigDecimal("100.00"), CurrencyCode.GBP) ;
        Money b = Money.of(new BigDecimal("200.00"), CurrencyCode.GBP) ;
        Money sum = a.add(b);

        assertThat(sum.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(sum.getCurrency()).isEqualTo(CurrencyCode.GBP);

    }

    @Test
    @DisplayName("should throw ex on adding different currencies")
    void shouldRejectOnAddingDifferentCurrency() {
          Money gbp = Money.of(new BigDecimal("100.00"), CurrencyCode.GBP) ;
          Money ngn = Money.of(new BigDecimal("150.00"), CurrencyCode.NGN) ;

          assertThatThrownBy(() ->
                  gbp.add(ngn))
                  .isInstanceOf(InconsistentCurrencyException.class)
                  .hasMessageContaining("Currencies must match");
    }

    @Test
    @DisplayName("two money object to have correct equality")
    void shouldHaveCorrectEquality() {

        Money m1 = Money.of(new BigDecimal("100.00"), CurrencyCode.EUR);
        Money m2 = Money.of(new BigDecimal("100.00"), CurrencyCode.EUR);
        Money m3 = Money.of(new BigDecimal("100.01"), CurrencyCode.EUR);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1).isNotEqualTo(m3);
        assertThat(m2).isNotEqualTo(m3);
        assertThat(m1).isNotEqualTo(null);
        assertThat(m1).isNotEqualTo("no money");
    }
}
