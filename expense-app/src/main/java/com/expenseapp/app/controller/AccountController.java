package com.expenseapp.app.controller;

import com.expenseapp.app.dto.accounts.ConnectedAccountsResponse;
import com.expenseapp.app.dto.response.AppResponse;
import com.expenseapp.app.interfaces.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = " Accounts", description = "Account APIs")
public class AccountController {
    private final AccountService accountService;

    @GetMapping()
    public ResponseEntity<AppResponse<ConnectedAccountsResponse>> getAccounts(){
        ConnectedAccountsResponse data =  accountService.getAccounts();

        var response = AppResponse.<ConnectedAccountsResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .success(true)
                .message("active account retrieved")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponse<Void>> disconnectAccount(@PathVariable UUID id) {
        accountService.disconnectAccount(id);
        var response = AppResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .success(true)
                .message("active account disconnected")
                .build();
        return ResponseEntity.ok(response);
    }
}
