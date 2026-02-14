package valueObjects;


import java.util.Objects;

/**
 * Immutable Value Object representing a transaction category.
 *
 * Examples: "Subscriptions", "Food & Drinks", "Transport", "Entertainment"
 *
 * Supports optional hierarchy via parent reference.
 * If user-customizable categories with persistent identity become needed later,
 * this can be promoted to an Entity.
 */
public final class Category {

    private final String name;
    private final Category parent;      // null = top-level category

    private Category(String name, Category parent) {
        this.name = normalizeName(Objects.requireNonNull(name, "Category name is required"));
        this.parent = parent;
    }

    public static Category of(String name) {
        return new Category(name, null);
    }

    public static Category of(String name, Category parent) {
        return new Category(name, Objects.requireNonNull(parent));
    }

    private static String normalizeName(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    public String getName() {
        return name;
    }

    public Category getParent() {
        return parent;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * Useful for display or reporting: "Food & Drinks > Coffee"
     */
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return name.equalsIgnoreCase(category.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
