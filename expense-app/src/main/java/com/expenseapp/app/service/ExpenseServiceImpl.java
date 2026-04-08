package com.expenseapp.app.service;

import com.domain.entities.TransactionEntity;
import com.domain.entities.User;
import com.domain.repositories.TransactionRepository;
import com.expenseapp.app.dto.expense.request.CreateExpenseRequest;
import com.domain.model.ExpenseRequestQuery;
import com.expenseapp.app.dto.expense.request.UpdateExpenseRequest;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import com.domain.model.PagedResponse;
import com.expenseapp.app.dto.response.ApiResponse;
import com.expenseapp.app.interfaces.ExpenseService;
import com.expenseapp.app.mapper.TransactionMapper;
import com.expenseapp.app.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    /**
     * @param request for filters
     * @return paged response
     */
    @Override
    public PagedResponse<ExpenseResponse> getExpenses(ExpenseRequestQuery request, UUID userId) {
        ExpenseRequestQuery query = getExpenseRequestQuery(request, userId);
        PagedResponse<TransactionEntity> result = transactionRepository.findAll(query);
        List<ExpenseResponse> responses = result.content().stream()
                .map(transactionMapper::toResponse)
                .toList();

        return new PagedResponse<>(
                responses,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.last()
        );

    }



    /**
     * @param expenseRequest
     * @return
     */
    @Override
    public ExpenseResponse createManualExpense(CreateExpenseRequest expenseRequest) {
        return null;
    }

    /**
     * @param id
     * @param updateExpenseRequest
     * @return
     */
    @Override
    public ExpenseResponse updateExpense(UUID id, UpdateExpenseRequest updateExpenseRequest) {
        return null;
    }

    /**
     * @param expenseId
     * @return
     */
    @Override
    public Void deleteExpense(UUID expenseId) {
        return null;
    }

    /**
     * @param expenseId
     * @return
     */
    @Override
    public ApiResponse<ExpenseResponse> getExpense(UUID expenseId) {
        return null;
    }







    @NotNull
    private ExpenseRequestQuery getExpenseRequestQuery(ExpenseRequestQuery request,UUID userId) {
        return new ExpenseRequestQuery(
                userId,
                request.emailAccountIds(),
                request.startDate(),
                request.endDate(),
                request.categories(),
                request.merchant(),
                request.minAmount(),
                request.maxAmount(),
                request.search(),
                request.page(),
                request.size(),
                request.sortBy(),
                request.sortDirection()
        );
    }
}
