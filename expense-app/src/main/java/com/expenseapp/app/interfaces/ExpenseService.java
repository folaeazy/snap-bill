package com.expenseapp.app.interfaces;

import com.expenseapp.app.dto.expense.request.CreateExpenseRequest;
import com.domain.model.ExpenseRequestQuery;
import com.expenseapp.app.dto.expense.request.UpdateExpenseRequest;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import com.domain.model.PagedResponse;
import com.expenseapp.app.dto.response.ApiResponse;
import java.util.UUID;

public interface ExpenseService {

    PagedResponse<ExpenseResponse> getExpenses(ExpenseRequestQuery requestQuery, UUID userId);
    ExpenseResponse createManualExpense(CreateExpenseRequest expenseRequest);
    ExpenseResponse updateExpense(UUID id ,UpdateExpenseRequest updateExpenseRequest);
    Void deleteExpense(UUID expenseId);

    //TODO : future implementation
    ApiResponse<ExpenseResponse> getExpense(UUID expenseId);
}
