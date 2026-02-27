package com.domain.entities;


import com.domain.enums.ConnectionStatus;
import com.domain.enums.EmailProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "")
@NoArgsConstructor
public class EmailAccount {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailProvider provider;           // GOOGLE, MICROSOFT, etc.

    @Column(nullable = false)
    private String providerEmail;             // the actual inbox email (e.g. user@gmail.com)

    @Column(length = 2048)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

    @Column(nullable = false)
    private Instant connectedAt = Instant.now();

    @Column
    private Instant lastSyncAt;

    @Column
    private Instant ExpiresAt;

   // status or error tracking
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status = ConnectionStatus.ACTIVE;

     //:last error message if sync failed
    @Column(length = 1000)
    private String lastErrorMessage;
}
