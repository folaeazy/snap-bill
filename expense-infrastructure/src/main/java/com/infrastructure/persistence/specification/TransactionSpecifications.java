package com.infrastructure.persistence.specification;

import com.domain.entities.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TransactionSpecifications {

    public static Specification<TransactionEntity> hasUser(UUID userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<TransactionEntity> dateBetween(
            LocalDate start,
            LocalDate end
    ) {
        return (root, query, cb) -> {
            if(start == null && end == null) {
                return null;
            }

            if(start != null && end != null ) {
                return cb.between(root.get("transactionDate"), start, end);
            }
            if(start != null) {
                return cb.greaterThanOrEqualTo(root.get("transactionDate"),start);
            }
            return cb.lessThanOrEqualTo(root.get("transactionDate"), end);
        };
    }

    public static Specification<TransactionEntity> emailAccountIn(List<UUID> accountIds) {
        return (root, query, cb) -> {
            if(accountIds == null || accountIds.isEmpty()) {
                return  null;
            }
            return root.get("emailAccount").get("id").in(accountIds);
        };
    }
}
