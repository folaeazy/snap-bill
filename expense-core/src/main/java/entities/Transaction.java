package entities;

import enums.TransactionSource;
import enums.TransactionType;
import valueObjects.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate Root representing a financial transaction.

 * This is the central entity of the domain: every change to a transaction
 * must go through this class to preserve invariants.

 * Supports both debits (expenses) and credits (refunds, income),
 * manual entry and AI-extracted transactions.
 */
public class Transaction {

    private final TransactionId id;                    // Identity
    private TransactionType type;                      // DEBIT, CREDIT, REFUND, TRANSFER, ...
    private Money amount;                              // Always positive; direction via type
    private TransactionDate date;
    private Merchant merchant;
    private Category category;
    private Set<Tag> tags = new HashSet<>();
    private BankAccountRef account;
    private Description description;                   // Raw narration / memo / remarks
    private TransactionSource source;
    private BigDecimal aiConfidence;                   // 0.0–1.0 from AI extraction (nullable)

    // Timestamp fields (for auditing / created/updated)
    private final Instant createdAt;
    private Instant updatedAt;


    // ───────────────────────────────────────────────
    //  Constructors
    // ──────────────────────────────────────────────-

    /**
     * Full constructor – used by repositories or factories when loading from persistence.
     */
    public Transaction(
            TransactionId id,
            TransactionType type,
            Money amount,
            TransactionDate date,
            Merchant merchant,
            Category category,
            Set<Tag> tags,
            BankAccountRef account,
            Description description,
            TransactionSource source,
            BigDecimal aiConfidence,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "Transaction ID is required");
        this.type = Objects.requireNonNull(type, "Transaction type is required");
        this.amount = Objects.requireNonNull(amount, "Amount is required");
        this.date = Objects.requireNonNull(date, "Date is required");
        this.merchant = merchant;           // can be null (e.g. cash withdrawal)
        this.category = category;           // can be null initially
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
        this.account = account;             // can be null
        this.description = description;     // can be null
        this.source = Objects.requireNonNull(source, "Source is required");
        this.aiConfidence = aiConfidence;   // can be null
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated timestamp required");

        validate();
    }

    /**
     * Factory method – most common way to create a new transaction (manual or from AI).
     */
    public static Transaction create(
            TransactionType type,
            Money amount,
            TransactionDate date,
            Merchant merchant,
            Category category,
            Set<Tag> tags,
            BankAccountRef account,
            Description description,
            TransactionSource source,
            BigDecimal aiConfidence
    ) {
        TransactionId id = TransactionId.generate();
    }
}
