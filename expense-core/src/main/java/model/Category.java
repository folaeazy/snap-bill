package model;


/**
 * Fixed set of expense categories with display names.
 * Used for reporting, filtering, and AI classification.
 */
public enum Category {

    FOOD_AND_DRINK("Food & Drink"),
    TRANSPORTATION("Transportation"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    TRAVEL("Travel"),
    BUSINESS("Business"),
    EDUCATION("Education"),
    HOUSING("Housing"),
    PERSONAL("Personal"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Helper for grouping
    public boolean isEssential() {
        return this == FOOD_AND_DRINK || this == UTILITIES || this == HOUSING || this == HEALTH;
    }
}
