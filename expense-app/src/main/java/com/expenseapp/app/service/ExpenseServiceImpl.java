package com.expenseapp.app.service;

import com.expenseapp.app.dto.expense.request.CreateExpenseRequest;
import com.expenseapp.app.dto.expense.request.ExpenseRequestQuery;
import com.expenseapp.app.dto.expense.request.UpdateExpenseRequest;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import com.expenseapp.app.dto.expense.response.PagedResponse;
import com.expenseapp.app.dto.response.ApiResponse;
import com.expenseapp.app.interfaces.ExpenseService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    /**
     * @param requestQuery
     * @return
     */
    @Override
    public PagedResponse<ExpenseResponse> getExpenses(ExpenseRequestQuery requestQuery) {
        return null;
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
}
