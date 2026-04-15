package com.expenseapp.app.mapper;

import com.domain.entities.TransactionEntity;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import com.expenseapp.app.util.DateFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final DateFormatter dateFormatter;

    public ExpenseResponse toResponse(TransactionEntity entity) {
        return ExpenseResponse.builder()
                .id(entity.getId())
                .merchant(resolveMerchant(entity))
                .category(resolveCategory(entity))
                .amount(entity.getAmount())
                .emailAccountEmail(entity.getEmailAccount().getProviderEmail())
                .date(entity.getTransactionDate())
                .displayDate(dateFormatter.formatDate(entity.getTransactionDateTime()))
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





}
