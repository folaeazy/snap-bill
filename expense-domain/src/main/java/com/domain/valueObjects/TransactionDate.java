package com.domain.valueObjects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;


/**
 * Value Object wrapping a transaction date (usually just date, sometimes with time).
 */
public final class TransactionDate {

    private final LocalDate date;
    private final LocalDateTime dateTime; // null if only date is known

    private TransactionDate(LocalDate date, LocalDateTime dateTime) {
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.dateTime = dateTime;
    }

    public static TransactionDate of(LocalDate date) {
        return new TransactionDate(date, null);
    }

    public static TransactionDate of(LocalDateTime dateTime) {
        return new TransactionDate(dateTime.toLocalDate(), dateTime);
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean hasTime() {
        return dateTime != null;
    }

    public boolean isAfter(TransactionDate other) {
        return this.date.isAfter(other.date);
    }

    public static TransactionDate now() {
        return of(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionDate that = (TransactionDate) o;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    @Override
    public String toString() {
        return hasTime() ? dateTime.toString() : date.toString();
    }
}
