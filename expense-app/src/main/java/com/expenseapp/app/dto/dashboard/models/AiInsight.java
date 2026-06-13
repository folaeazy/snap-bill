package com.expenseapp.app.dto.dashboard.models;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Builder;

@Builder
public record AiInsight(
        String type,
        String message
) {
}
