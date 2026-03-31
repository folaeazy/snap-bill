package com.expenseapp.app.dto.report.request;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record ReportQueryRequest(
        List<UUID> emailAccountIds,
        LocalDate startDate,
        LocalDate endDate
) { }
