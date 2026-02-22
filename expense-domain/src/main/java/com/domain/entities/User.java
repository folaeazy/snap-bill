package com.domain.entities;

import com.domain.enums.AuthProvider;
import com.domain.valueObjects.CurrencyCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Column(name = "password_hash")
    private String passwordHash; // bcrypt, nullable if OAuth-only

    @Column(nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    private Set<String> authorities = Set.of("USER"); // default role

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
