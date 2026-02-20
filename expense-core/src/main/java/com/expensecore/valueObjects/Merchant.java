package com.expensecore.valueObjects;

import java.util.Objects;

/**
 * Value Object for merchant/payee information.
 */
public final class Merchant {

    private final String name;
    private final String normalizedName; // e.g. "Spotify" instead of "SPOTIFY INC PAYPAL"

    public Merchant(String name, String normalizedName) {
        this.name = Objects.requireNonNull(name, "Merchant name cannot be null").trim();
        this.normalizedName = normalizedName != null ? normalizedName.trim() : this.name;
    }

    public static Merchant of(String name) {
        return new Merchant(name, null);
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Merchant merchant = (Merchant) o;
        return normalizedName.equals(merchant.normalizedName);
    }

    @Override
    public int hashCode() {
        return normalizedName.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
