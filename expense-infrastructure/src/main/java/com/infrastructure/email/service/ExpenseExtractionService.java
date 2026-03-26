package com.infrastructure.email.service;

import com.domain.entities.RawEmailMessage;
import com.domain.domain.Transaction;
import com.domain.gateways.AiGateway;
import com.domain.model.ExtractionResult;
import com.domain.model.ValidationResult;
import com.infrastructure.Ai.ExpensePromptBuilder;
import com.infrastructure.email.Components.ExtractionValidator;
import com.infrastructure.email.Components.TransactionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseExtractionService {

    private final ExpensePromptBuilder promptBuilder;
    private final AiGateway aiGateway;
    private final ExtractionValidator validator;
    private final TransactionFactory transactionFactory;
    private final TextCleaner textCleaner;

    public Optional<Transaction> extract(RawEmailMessage email) {

        //clean text
        String cleanedEmail = textCleaner.clean(email);

        // build prompt
        String prompt = promptBuilder.build(cleanedEmail);

        // Ai extraction
        ExtractionResult result = aiGateway.extractExpenses(prompt);
        ValidationResult validation = validator.validate(result);
        if (!validation.valid()) {
            log.warn("Validation failed for email {}: {}",
                    email.getId(), validation.reason());
            return Optional.empty();
        }

        // build transaction
        Transaction tx = transactionFactory.fromExtraction(result);
        return Optional.of(tx);

    }
}
