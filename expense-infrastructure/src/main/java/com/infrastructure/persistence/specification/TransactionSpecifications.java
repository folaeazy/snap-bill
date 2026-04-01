package com.infrastructure.persistence.specification;

import com.domain.entities.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
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

    public static Specification<TransactionEntity> categoryIn(List<String> categories) {
        return (root, query, cb) -> {
            if(categories == null || categories.isEmpty()) {
                return null;
            }
            return root.get("category").in(categories);
        };
    }

    public static Specification<TransactionEntity> merchantContains(String merchant) {
        return (root, query, cb) ->  {
            if(merchant == null || merchant.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("merchant")),
                    "%" + merchant.toLowerCase() + "%") ;
        };
    }

    public static Specification<TransactionEntity> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if(min == null && max == null) {
                return null;
            }
            if(min != null && max != null) {
                return cb.between(root.get("amount"), min, max);
            }

            if(min != null) {
                return cb.greaterThanOrEqualTo(root.get("amount"), min);
            }

            return cb.lessThanOrEqualTo(root.get("amount"), max);
        };
    }

    public static Specification<TransactionEntity> search(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("merchant")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }



}
