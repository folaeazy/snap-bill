package valueObjects;

import java.util.Objects;

/**
 * Simple immutable Value Object for tags / labels.
 *
 * Used for flexible, user- or AI-added classification beyond categories.
 * Examples: "Recurring", "Business", "Urgent", "2026-trip", "Refundable"
 */
public final class Tag {

    private final String value;

    public Tag(String value) {
        this.value = normalize(Objects.requireNonNull(value, "Tag value is required"));
    }

    public static Tag of(String value) {
        return new Tag(value);
    }

    private static String normalize(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be empty");
        }
        // Optional: decide on case policy
        // Many apps normalize to lowercase for consistency
        return trimmed.toLowerCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return value.equalsIgnoreCase(tag.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
