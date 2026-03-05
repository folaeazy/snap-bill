package com.infrastructure.email.Components;

import com.domain.model.EmailMessageDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class FinancialEmailDetector {
    private static final List<String> FINANCIAL_KEYWORDS = List.of(
            "debited",
            "credited",
            "transaction",
            "payment",
            "transfer",
            "withdrawal",
            "deposit",
            "spent",
            "received",
            "purchase",
            "POS",
            "ATM",
            "alert"
    );

    private static final List<String> FINANCIAL_SENDERS = List.of(
            "alert",
            "bank",
            "transaction",
            "payment",
            "noreply",
            "no-reply",
            "no_reply",
            "receipt"
    );

    public List<EmailMessageDto> filterFinancialCandidate (List<EmailMessageDto> messages) {
        messages.stream()
                .filter(this::looksFinancial)
                .toList();
    }

    private boolean looksFinancial(EmailMessageDto emailMessageDto) {

        String subject = safe(emailMessageDto.getSubject());
        String snippet = safe(emailMessageDto.getSnippet());
        String from = safe(emailMessageDto.getFrom());

        String combined = (subject + " " + snippet).toLowerCase();

        boolean keywordMatch = FINANCIAL_KEYWORDS.stream()
                .anyMatch(combined::contains);

        boolean senderMatch = FINANCIAL_SENDERS.stream()
                .anyMatch(from.toLowerCase()::contains);

        return keywordMatch || senderMatch;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
