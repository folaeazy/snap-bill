package com.domain.valueObjects;

import java.time.*;
import java.util.Objects;


/**
 * Value Object wrapping a transaction date (usually just date, sometimes with time).
 */
public final class TransactionDate {

    private final LocalDate date;
    private final Instant dateTime; // null if only date is known
    private final ZoneId zone;

    private TransactionDate(LocalDate date, Instant dateTime, ZoneId zone) {
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.dateTime = dateTime;
        this.zone = zone;
    }

    public static TransactionDate of(LocalDate date) {
        return new TransactionDate(date, null, null);
    }

    public static TransactionDate of(Instant dateTime, ZoneId zone) {
        Objects.requireNonNull(dateTime, "Instant cannot be null");
        Objects.requireNonNull(zone, "ZoneId cannot be null");
        LocalDate derivedDate = dateTime.atZone(zone).toLocalDate();
        return new TransactionDate(derivedDate, null, null);
    }

    public static TransactionDate of(Instant dateTime) {
        Objects.requireNonNull(dateTime, "Instant cannot be null");

        // Fallback LocalDate using UTC
        LocalDate derivedDate = dateTime.atZone(ZoneOffset.UTC).toLocalDate();

        return new TransactionDate(derivedDate, dateTime, null);
    }

    public LocalDate getDate() {
        return date;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public ZoneId getZone() {
        return zone;
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

    /**
     * Returns ZonedDateTime for display purposes
     */
    public ZonedDateTime toZonedDateTime() {
        if (dateTime == null || zone == null) {
            return null;
        }
        return dateTime.atZone(zone);
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
