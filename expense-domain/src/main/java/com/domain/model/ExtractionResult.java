package com.domain.model;

import java.util.List;

public record ExtractionResult(
        List<ParsedExpense> extractedExpenses,
        double overallConfidence,     // 0.0 to 1.0
        String rawAiReasoning,        // for debugging / user review
        String errorMessage           // null if successful
) {
}
