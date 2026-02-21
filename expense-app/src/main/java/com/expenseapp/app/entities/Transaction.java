package com.expenseapp.app.entities;


import com.expenseapp.app.entities.embenddables.BankAccountRefEmbeddable;
import com.expenseapp.app.entities.embenddables.MoneyEmbeddable;
import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
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
@NoArgsConstructor
@Setter
@Getter
@Table(name = "transactions")
public class Transaction {

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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "amount_currency"))
    })
    private MoneyEmbeddable amount;

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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "accountId", column = @Column(name = "bank_account_id")),
            @AttributeOverride(name = "label", column = @Column(name = "bank_label")),
            @AttributeOverride(name = "last4", column = @Column(name = "bank_last4")),
            @AttributeOverride(name = "currency", column = @Column(name = "bank_currency"))
    })
    private BankAccountRefEmbeddable bankAccount;

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
