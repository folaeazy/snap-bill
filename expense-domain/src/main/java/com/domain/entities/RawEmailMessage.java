package com.domain.entities;

import com.domain.enums.EmailProvider;
import com.domain.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "raw_emails", indexes = {
            @Index(name = "idx_email_account_received", columnList = "email_account_id, received_date"),

            @Index(name = "idx_processing_status", columnList = "processing_status")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provider_message",
                columnNames = {"provider_message_id", "email_account_id"}
        )
)
public class RawEmailMessage {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_account_id", nullable = false)
    private EmailAccount emailAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailProvider provider;

    @Column(name = "thread_id")
    private String threadId;

    // Gmail message ID (used for deduplication)
    @Column(name = "provider_message_id", nullable = false, unique = true)
    private String providerMessageId;

    private String subject;

    private String from;

    private String to;

    @Column(name = "received_date")
    private Instant receivedDate;

    @Column(length = 1000)
    private String snippet;

    @Column(columnDefinition = "text")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    // attachments metadata
    @ElementCollection
    @CollectionTable(name = "raw_email_attachments")
    @Column(name = "attachment_name")
    private List<String> attachments = new ArrayList<>();

    // AI pipeline state
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private ProcessingStatus processed = ProcessingStatus.PENDING;

    @Column(nullable = false)
    private Instant fetchedAt = Instant.now();

}
