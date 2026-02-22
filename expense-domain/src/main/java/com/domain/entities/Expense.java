package com.domain.entities;

import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
import com.domain.valueObjects.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column
    private Instant transactionDateTime;   // optional full timestamp

    @Column
    private String merchantName;

    @Column
    private String categoryName;

    @ElementCollection
    @CollectionTable(name = "transaction_tags", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Flat Money fields
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private CurrencyCode currency;

    // Flat BankAccountRef fields
    @Column(name = "bank_account_id")
    private String bankAccountId;

    @Column(name = "bank_label")
    private String bankLabel;

    @Column(name = "bank_last4", length = 4)
    private String bankLast4;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_currency", length = 3)
    private CurrencyCode bankCurrency;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source;

    @Column(precision = 5, scale = 4)
    private BigDecimal aiConfidence;       // 0.00 to 1.00

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
