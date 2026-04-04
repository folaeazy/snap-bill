package com.infrastructure.persistence.implementation;

import com.domain.entities.TransactionEntity;
import com.domain.model.ExpenseRequestQuery;
import com.domain.model.PagedResponse;
import com.domain.repositories.TransactionRepository;
import com.infrastructure.persistence.repositories.SpringDataTransactionRepository;
import com.infrastructure.persistence.specification.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {
    private final SpringDataTransactionRepository jpASpr;

    @Override
    public TransactionEntity save(TransactionEntity expense) {
        return jpASpr.save(expense);
    }


    @Override
    public Optional<TransactionEntity> findById(UUID id) {
        return jpASpr.findById(id);
    }

    @Override
    public PagedResponse<TransactionEntity> findAll(ExpenseRequestQuery query) {
        //build specification
        Specification<TransactionEntity> spec = buildSpecification(query);
        Pageable pageable = buildPageable(query);
        Page<TransactionEntity> page = jpASpr.findAll(spec, pageable);
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );

    }



    @Override
    public void deleteById(UUID id) {
        jpASpr.deleteById(id);
    }


    //helper method
    private Specification<TransactionEntity> buildSpecification(ExpenseRequestQuery query) {
        return Specification
                .where(TransactionSpecifications.hasUser(query.userId()))
                .and(TransactionSpecifications.emailAccountIn(query.emailAccountIds()))
                .and(TransactionSpecifications.dateBetween(query.startDate(), query.endDate()))
                .and(TransactionSpecifications.categoryIn(query.categories()))
                .and(TransactionSpecifications.merchantContains(query.merchant()))
                .and(TransactionSpecifications.amountBetween(query.minAmount(), query.maxAmount()))
                .and(TransactionSpecifications.search(query.search()));
    }

    private Pageable buildPageable(ExpenseRequestQuery query) {
        Sort sort = buildSort(query);
        return PageRequest.of(
                query.page(),
                query.size(),
                sort
        );

    }


    private Sort buildSort(ExpenseRequestQuery query) {

        // Default sorting
        if (query.sortBy() == null || query.sortBy().isBlank()) {
            return Sort.by(Sort.Direction.DESC, "transactionDateTime");
        }

        Sort.Direction direction =
                "asc".equalsIgnoreCase(query.sortDirection())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(direction, mapSortField(query.sortBy()));
    }


    private String mapSortField(String sortBy) {

        return switch (sortBy) {
            case "amount" -> "amount";
            case "merchant" -> "merchant";
            default -> "transactionDateTime";
        };
    }
}
