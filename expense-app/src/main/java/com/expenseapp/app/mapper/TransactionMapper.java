package com.expenseapp.app.mapper;

import com.domain.entities.TransactionEntity;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class TransactionMapper {

    public ExpenseResponse toResponse(TransactionEntity entity) {
        return ExpenseResponse.builder()
                .id(entity.getId())
                .merchant(resolveMerchant(entity))
                .category(resolveCategory(entity))
                .amount(entity.getAmount())
                .emailAccountEmail(entity.getEmailAccount().getProviderEmail())
                .date(entity.getTransactionDate())
                .displayDate(formatDate(entity.getTransactionDateTime()))
                .build();

    }

    private String resolveMerchant(TransactionEntity entity) {
        return entity.getMerchant() != null ? entity.getMerchant() : "Unknown";
    }

    private String resolveCategory(TransactionEntity entity) {
        return entity.getCategory() != null ? entity.getCategory() : "Other";
    }

//    private String resolveAccountEmail(TransactionEntity entity) {
//        return entity.getEmailAccount() != null
//                ? entity.getEmailAccount().getEmail()
//                : null;
//    }

    private String formatDate(Instant instant) {

        if (instant == null) return null;

        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();

        if (zdt.toLocalDate().equals(now.toLocalDate())) {
            return "Today, " + zdt.toLocalTime().withSecond(0).withNano(0);
        }

        return zdt.getMonth() + " " + zdt.getDayOfMonth() + ", " +
                zdt.toLocalTime().withSecond(0).withNano(0);
    }
}
