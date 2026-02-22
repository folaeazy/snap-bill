package com.domain.valueObjects;


/**
 * Enum of supported ISO 4217 currency codes.
 * Start small (expand as needed).
 */
public enum CurrencyCode {

    NGN("NGN"), // Nigerian Naira – your primary focus
    USD("USD"),
    EUR("EUR"),
    GBP("GBP");

    private final String code;

    CurrencyCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CurrencyCode fromCode(String code) {
        for (CurrencyCode c : values()) {
            if (c.code.equalsIgnoreCase(code)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }
}
