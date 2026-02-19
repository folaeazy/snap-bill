package com.expenseapp.app.entities;

import com.expenseapp.app.emuns.AuthProvider;
import com.expensecore.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private String providerUserId;

    @Column
    private String name;        // from OAuth profile

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // One user can have multiple connected email accounts
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailAccount> emailAccounts = new ArrayList<>();

    // Optional: default currency (for future multi-currency display/aggregation)
    @Enumerated(EnumType.STRING)
    @Column
    private CurrencyCode defaultCurrency = CurrencyCode.NGN;

    // Helper method
    public void addEmailAccount(EmailAccount account) {
        emailAccounts.add(account);
        account.setUser(this);
    }

    public void removeEmailAccount(EmailAccount account) {
        emailAccounts.remove(account);
        account.setUser(null);
    }



}
