package com.infrastructure.email.Components;

import com.domain.entities.Transaction;
import com.domain.model.ExtractionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionFactory {

    public Transaction fromExtraction(ExtractionResult result) {
        return null;
    }
}
