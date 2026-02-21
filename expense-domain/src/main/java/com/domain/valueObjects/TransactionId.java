package com.domain.valueObjects;

import java.util.Objects;
import java.util.UUID;


/**
 * Value Object representing the unique identity of a Transaction.
 * Uses UUID for global uniqueness.
 */
public final class TransactionId {

    private final UUID uuid;

    private TransactionId(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "UUId cannot be null");
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }

    public static TransactionId fromString(String uuidString) {
        Objects.requireNonNull(uuidString, "UUID string cannot be null");
        try {
            return new TransactionId(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
        }
    }

    public UUID getValue() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId that = (TransactionId) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return uuid.toString();
    }


}
