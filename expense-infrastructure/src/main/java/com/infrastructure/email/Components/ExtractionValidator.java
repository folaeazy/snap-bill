package com.infrastructure.email.Components;

import com.domain.model.ExtractionResult;
import com.domain.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExtractionValidator {

    public ValidationResult validate(ExtractionResult result) {

        if (result == null) return ValidationResult.failure("result is null");

        if (!result.transaction()) return ValidationResult.failure("Not a transaction");

        if (isBlank(result.merchant())) return ValidationResult.failure("missing merchant");

        if (result.amount() == null || result.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("Invalid amount");
        }

        if (isBlank(String.valueOf(result.currency()))) return ValidationResult.failure("missing currency");

        return ValidationResult.success();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
