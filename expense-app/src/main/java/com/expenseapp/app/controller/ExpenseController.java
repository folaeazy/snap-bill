package com.expenseapp.app.controller;

import com.domain.entities.User;
import com.expenseapp.app.dto.expense.request.CreateExpenseRequest;
import com.domain.model.ExpenseRequestQuery;
import com.expenseapp.app.dto.expense.request.UpdateExpenseRequest;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import com.domain.model.PagedResponse;
import com.expenseapp.app.dto.response.ApiResponse;
import com.expenseapp.app.interfaces.ExpenseService;
import com.expenseapp.app.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getExpenses(@Valid @ModelAttribute ExpenseRequestQuery requestQuery) {
        UUID userId =  UUID.fromString("395045ce-4920-45f8-bd34-507955e08f14");//getCurrentUser().getId();
        var result =  expenseService.getExpenses(requestQuery, userId);
        PagedResponse<ExpenseResponse> pagedResponse = new PagedResponse<>(
                result.content(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.last()
        );

        ApiResponse<PagedResponse<ExpenseResponse>> response = ApiResponse.<PagedResponse<ExpenseResponse>>builder()
                .success(true)
                .timestamp(Instant.now())
                .message("Expenses retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .data(pagedResponse)
                .build();

        return ResponseEntity.ok(response);
    }


    //  CREATE  A MANUAL EXPENSE
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @RequestBody CreateExpenseRequest request
    ) {
        expenseService.createManualExpense(request);
        ApiResponse<ExpenseResponse> response = ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .timestamp(Instant.now())
                .message("Expense created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .build();
        return ResponseEntity.ok(response);
    }

    //  UPDATE AN EXPENSE
    @PutMapping("/{id}")
    public  ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable UUID id,
            @RequestBody UpdateExpenseRequest request
    ) {

        expenseService.updateExpense(id, request);
        ApiResponse<ExpenseResponse> response = ApiResponse.<ExpenseResponse>builder()
                .success(true)
                .timestamp(Instant.now())
                .message("Expense updated successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);

    }

    //  DELETE AN EXPENSE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable UUID id){

        expenseService.deleteExpense(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(true)
            .timestamp(Instant.now())
            .message("Expense deleted successfully")
            .statusCode(HttpStatus.NO_CONTENT.value())
            .build();
        return ResponseEntity.ok(response);

    }


    //============Helper method-===================//

}
