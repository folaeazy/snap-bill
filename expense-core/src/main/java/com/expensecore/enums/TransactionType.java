package com.expensecore.enums;


/**
 * Classifies the financial nature/direction of a transaction.
 *
 * Most expense-tracking apps primarily care about DEBIT (money going out).
 * Including CREDIT/REFUND/INCOME makes the model more future-proof.
 */
public enum TransactionType {

    /**
     * Money leaving the account (purchase, bill payment, transfer out, subscription charge)
     * → This is what most people think of as an "expense".
     */
    DEBIT,

    /**
     * Money entering the account (salary, refund, transfer in, gift, cashback)
     */
    CREDIT,

    /**
     * A refund or reversal of a previous debit (often linked to original transaction)
     */
    REFUND,

    /**
     * Transfer between your own accounts (usually zero net effect on total wealth)
     */
    TRANSFER,

    /**
     * Other rare cases (adjustment, interest, fee, etc.)
     * Can be used as catch-all or split later.
     */
    OTHER
}
