package com.expenseapp.app.controller;

import com.expenseapp.app.dto.expense.request.CreateExpenseRequest;
import com.domain.model.ExpenseRequestQuery;
import com.expenseapp.app.dto.expense.request.UpdateExpenseRequest;
import com.expenseapp.app.dto.expense.response.ExpenseResponse;
import com.domain.model.PagedResponse;
import com.expenseapp.app.dto.response.AppResponse;
import com.expenseapp.app.interfaces.ExpenseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expense")
@Tag(name = " Expense", description = "Expense APIs")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUser currentUser;

    @GetMapping("/")
    public ResponseEntity<AppResponse<PagedResponse<ExpenseResponse>>> getExpenses(@Valid @ModelAttribute ExpenseRequestQuery requestQuery) {
        UUID userId =  currentUser.getCurrentUser().getId();
        var result =  expenseService.getExpenses(requestQuery, userId);
        PagedResponse<ExpenseResponse> pagedResponse = new PagedResponse<>(
                result.content(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.last()
        );

        AppResponse<PagedResponse<ExpenseResponse>> response = AppResponse.<PagedResponse<ExpenseResponse>>builder()
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
    public ResponseEntity<AppResponse<ExpenseResponse>> createExpense(
            @RequestBody CreateExpenseRequest request
    ) {
        expenseService.createManualExpense(request);
        AppResponse<ExpenseResponse> response = AppResponse.<ExpenseResponse>builder()
                .success(true)
                .timestamp(Instant.now())
                .message("Expense created successfully")
                .statusCode(HttpStatus.CREATED.value())
                .build();
        return ResponseEntity.ok(response);
    }

    //  UPDATE AN EXPENSE
    @PutMapping("/{id}")
    public  ResponseEntity<AppResponse<ExpenseResponse>> updateExpense(
            @PathVariable UUID id,
            @RequestBody UpdateExpenseRequest request
    ) {

        expenseService.updateExpense(id, request);
        AppResponse<ExpenseResponse> response = AppResponse.<ExpenseResponse>builder()
                .success(true)
                .timestamp(Instant.now())
                .message("Expense updated successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);

    }

    //  DELETE AN EXPENSE
    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponse<Void>> deleteExpense(@PathVariable UUID id){

        expenseService.deleteExpense(id);
        AppResponse<Void> response = AppResponse.<Void>builder()
            .success(true)
            .timestamp(Instant.now())
            .message("Expense deleted successfully")
            .statusCode(HttpStatus.NO_CONTENT.value())
            .build();
        return ResponseEntity.ok(response);

    }


    //============Helper method-===================//

}
