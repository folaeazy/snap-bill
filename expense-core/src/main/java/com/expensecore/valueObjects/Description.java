package com.expensecore.valueObjects;

public final class Description {

    private final String text;

    private Description(String text) {
        this.text = text != null ? text.trim() : "";
    }

    public static Description of(String text) {
        return new Description(text);
    }

    public static Description empty() {
        return new Description("");
    }

    public String getText() {
        return text;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Description that = (Description) o;
        return text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text;
    }

}
