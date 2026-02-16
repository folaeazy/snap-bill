package valueObjects;

import enums.CurrencyCode;
import exceptions.InconsistentCurrencyException;
import exceptions.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object for monetary amounts with currency.
 * Amounts are always stored with 2 decimal places (common for most currencies).
 */

public final class Money {

    private final BigDecimal amount;
    private final CurrencyCode currency;

    private Money(BigDecimal amount, CurrencyCode currency) {
        BigDecimal normalizedAmount = Objects.requireNonNull(amount, "Amount cannot be null")
                .setScale(2, RoundingMode.HALF_EVEN);

        if (normalizedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Money amount cannot be negative");
        }

        this.amount = normalizedAmount;
        this.currency = Objects.requireNonNull(currency, "currency cannot be null");
    }

    public static Money of(BigDecimal amount, CurrencyCode currency) {
        return new Money(amount, currency);
    }

    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isZeroOrNegative() {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }


    // Basic arithmetic (returns new instance)
    public Money add(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        checkSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    private void checkSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new InconsistentCurrencyException(
                    "Currencies must match: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency.getCode();
    }
}
