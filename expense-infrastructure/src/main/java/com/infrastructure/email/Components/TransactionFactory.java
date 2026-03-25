package com.infrastructure.email.Components;

import com.domain.entities.Transaction;
import com.domain.enums.TransactionSource;
import com.domain.enums.TransactionType;
import com.domain.model.ExtractionResult;
import com.domain.valueObjects.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        return Transaction.create(
                type,
                money,
                TransactionDate.of(LocalDate.from(result.transaction_date())),
                Merchant.of(result.merchant()),
                category,
                Set.of(),
                null,
                Description.of("This is a Ai extracted"),
                TransactionSource.EMAIL_GMAIL,
                BigDecimal.valueOf(0.9)

        );
    }
}
