package com.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
public record ExpenseRequestQuery(
        List<UUID> emailAccountIds,

        LocalDate startDate,
        LocalDate endDate,

        List<String> categories,

        String merchant,   // search/filter

        BigDecimal minAmount,
        BigDecimal maxAmount,

        String search,     // global search

        Integer page,
        Integer size,

        String sortBy,
        String sortDirection
) { }
