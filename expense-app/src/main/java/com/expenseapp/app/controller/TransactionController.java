package com.expenseapp.app.controller;

import com.expenseapp.app.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<String>> getTransactions(){
        ApiResponse<String> response = ApiResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .success(true)
                .message("Getting transaction")
                .build();
        return ResponseEntity.ok(response);
    }
}
