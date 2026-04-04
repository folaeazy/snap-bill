package com.infrastructure.mapper;

import com.domain.domain.Transaction;

import com.domain.entities.EmailAccount;
import com.domain.entities.TransactionEntity;
import com.domain.entities.User;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public TransactionEntity toEntity(User user, EmailAccount emailAccount, Transaction tx) {

        TransactionEntity entity = new TransactionEntity();
        entity.setType(tx.getType());
        entity.setAmount(tx.getAmount().getAmount());
        entity.setCurrency(tx.getAmount().getCurrency());
        entity.setMerchant(tx.getMerchant().getName());
        entity.setCategory(tx.getCategory().getName());
        entity.setSource(tx.getSource());
        entity.setTransactionDateTime(tx.getDate().getDateTime());
        entity.setTransactionDate(tx.getDate().getDate());
        entity.setOriginalZone(tx.getDate().getZone() != null ? tx.getDate().getZone().getId() : null);
        entity.setAiConfidence(tx.getAiConfidence());
        entity.setUser(user);
        entity.setEmailAccount(emailAccount);

        return entity;
    }

}






