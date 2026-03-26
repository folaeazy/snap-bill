package com.infrastructure.mapper;

import com.domain.domain.Transaction;

import com.domain.entities.TransactionEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TransactionMapper {

    public TransactionEntity toEntity(Transaction tx) {

        TransactionEntity entity = new TransactionEntity();
        entity.setType(tx.getType().name());
        entity.setAmount(tx.getAmount().getAmount());
        entity.setCurrency(tx.getAmount().getCurrency().name());
        entity.setMerchant(tx.getMerchant().getName());
        entity.setCategory(tx.getCategory().getName());
        entity.setSource(tx.getSource().name());
        entity.setTransactionDate(Instant.from(tx.getDate().getDateTime()));
        entity.setAiConfidence(tx.getAiConfidence());

        return entity;
    }
}
