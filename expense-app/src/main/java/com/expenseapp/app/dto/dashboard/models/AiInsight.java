package com.expenseapp.app.dto.dashboard.models;

import lombok.Builder;

@Builder
public record AiInsight(
        String type,
        String message
) {
}
