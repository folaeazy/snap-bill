package com.infrastructure.email.Components;

import com.domain.enums.TransactionType;
import com.domain.model.ExtractionResult;
import org.springframework.stereotype.Component;

@Component
public class TransactionTypeResolver {

    public TransactionType resolve(ExtractionResult result) {
        if(result == null) {
            return TransactionType.DEBIT;
        }

        String text = (result.merchant() + " " + result.category()).toLowerCase();

        //Detecting transaction type
        if(containsAny(text, "credited", "received", "credit")) {
            return TransactionType.CREDIT;
        }

        if(containsAny(text, "reversal", "refund")) {
            return TransactionType.REFUND;
        }

        //Default to debit
        return TransactionType.DEBIT;


    }

    //: Helper function
    private boolean containsAny(String text, String... keywords) {
        for(String keyword : keywords) {
            if(text.contains(keyword)) return true;
        }
        return false;
    }
}
