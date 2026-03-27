package com.infrastructure.email.Components;

import com.domain.domain.Transaction;
import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
import com.domain.model.ExtractionResult;
import com.domain.valueObjects.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TransactionFactory {

    private final CategoryTypeResolver categoryTypeResolver;
    private final TransactionTypeResolver transactionTypeResolver;

    public Transaction fromExtraction(ExtractionResult result) {
        Money money = Money.of(result.amount(), result.currency());

        TransactionType type = transactionTypeResolver.resolve(result);
        Category category = categoryTypeResolver.resolve(result.category());
        Instant llmInstant = Instant.parse(result.transactionDate());


        return Transaction.create(
                type,
                money,
                TransactionDate.of(llmInstant),
                Merchant.of(result.merchant()),
                category,
                Set.of(),
                null,
                Description.of("This is a Ai extracted"),
                TransactionSource.EMAIL_GMAIL, // TODO: might be extended in the future
                BigDecimal.valueOf(0.9)

        );
    }
}
