package com.expensecore.enums;

public enum TransactionSource {
    MANUAL,             // user entered directly
    EMAIL_GMAIL,
    EMAIL_OUTLOOK,
    EMAIL_OTHER,
    BANK_API,           // future Open Banking / Plaid-like
    CSV_IMPORT,
    OTHER
}
