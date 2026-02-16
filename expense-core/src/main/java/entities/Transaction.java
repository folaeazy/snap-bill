package entities;

import enums.TransactionSource;
import enums.TransactionType;
import exceptions.DomainValidationException;
import exceptions.InconsistentCurrencyException;
import exceptions.InvalidAmountException;
import exceptions.MissingRequiredFieldException;
import valueObjects.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
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
        Instant now = Instant.now();

        return new Transaction(id, type, amount, date, merchant, category,
                tags, account, description, source, aiConfidence,
                now, now);
    }

    // ───────────────────────────────────────────────
    //  Business methods (with immutability style)
    // ───────────────────────────────────────────────

    public Transaction withType(TransactionType newType) {
        Transaction copy = copy();
        copy.type = Objects.requireNonNull(newType);
        copy.updatedAt = Instant.now();
        copy.validate();
        return copy;

    }

    public Transaction withAmount(Money newAmount) {
        Transaction copy = copy();
        copy.amount = Objects.requireNonNull(newAmount);
        copy.updatedAt = Instant.now();
        copy.validate();
        return copy;
    }

    public Transaction withDate(TransactionDate newDate) {
        Transaction copy = copy();
        copy.date = Objects.requireNonNull(newDate);
        copy.updatedAt = Instant.now();
        copy.validate();
        return copy;
    }

    public Transaction withMerchant(Merchant newMerchant) {
        Transaction copy = copy();
        copy.merchant = newMerchant; // nullable
        copy.updatedAt = Instant.now();
        copy.validate();
        return copy;
    }

    public Transaction withCategory(Category newCategory) {
        Transaction copy = copy();
        copy.category = newCategory; // nullable
        copy.updatedAt = Instant.now();
        return copy;
    }

    public Transaction addTag(Tag tag) {
        Objects.requireNonNull(tag);
        Transaction copy = copy();
        copy.tags.add(tag);
        copy.updatedAt = Instant.now();
        return copy;
    }

    public Transaction removeTag(Tag tag) {
        Transaction copy = copy();
        copy.tags.remove(tag);
        copy.updatedAt = Instant.now();
        return copy;
    }

    public Transaction withDescription(Description newDescription) {
        Transaction copy = copy();
        copy.description = newDescription; // nullable
        copy.updatedAt = Instant.now();
        return copy;
    }

    public Transaction withAccount(BankAccountRef newAccount) {
        Transaction copy = copy();
        copy.account = newAccount; // nullable
        copy.updatedAt = Instant.now();
        copy.validate();
        return copy;
    }


    // ───────────────────────────────────────────────
    //  Validation (business invariants)
    // ───────────────────────────────────────────────

    private void validate() {
        if (amount == null || amount.isZeroOrNegative()) {
            throw new InvalidAmountException("Amount must be positive");
        }

        if (type == null) {
            throw new MissingRequiredFieldException("Transaction type is required");
        }

        if (date == null) {
            throw new MissingRequiredFieldException("Transaction date is required");
        }

        // Optional: prevent future dates for most cases (can be overridden in subclasses or config)
        if (date.isAfter(TransactionDate.now()) && type == TransactionType.DEBIT) {
            throw new DomainValidationException("Debit transactions cannot have future dates");
        }

        // Currency consistency (if account is set)
        if (account != null && !account.getCurrency().equals(amount.getCurrency())) {
            throw new InconsistentCurrencyException(
                    "Transaction currency (" + amount.getCurrency() +
                            ") does not match account currency (" + account.getCurrency() + ")"
            );
        }

        // More rules can be added later (e.g. recurring pattern validation, max amount per type, etc.)
    }


    // ───────────────────────────────────────────────
    //  Getters (immutable view)
    // ───────────────────────────────────────────────

    public TransactionId getId() { return id; }
    public TransactionType getType() { return type; }
    public Money getAmount() { return amount; }
    public TransactionDate getDate() { return date; }
    public Merchant getMerchant() { return merchant; }
    public Category getCategory() { return category; }
    public Set<Tag> getTags() { return Collections.unmodifiableSet(tags); }
    public BankAccountRef getAccount() { return account; }
    public Description getDescription() { return description; }
    public TransactionSource getSource() { return source; }
    public BigDecimal getAiConfidence() { return aiConfidence; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Convenience methods
    public boolean isDebit() { return type == TransactionType.DEBIT; }
    public boolean isCredit() { return type == TransactionType.CREDIT; }

    // ───────────────────────────────────────────────
    //  Identity & equality (based on ID only)
    // ───────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + type +
                ", amount=" + amount +
                ", date=" + date +
                ", merchant=" + merchant +
                ", category=" + category +
                ", source=" + source +
                '}';
    }


    // Internal copy helper for withXxx pattern
    private Transaction copy() {
        return new Transaction(
                id, type, amount, date, merchant, category,
                new HashSet<>(tags), account, description, source, aiConfidence,
                createdAt, updatedAt
        );
    }
}
