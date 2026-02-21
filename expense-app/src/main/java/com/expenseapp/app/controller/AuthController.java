package com.expenseapp.app.controller;

import com.expenseapp.app.dto.response.ApiResponse;
import com.expenseapp.app.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils; // your JWT util

    // Temporary dev endpoint - remove in production
    @GetMapping("/test-token")
    public ResponseEntity<ApiResponse<String>> getTestToken() {
        String testEmail = "freshmailer36@gmail.com";
        String token = jwtUtils.generateToken(testEmail);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .success(true)
                .message("Test JWT issued for development")
                .data(token)
                .build();

        return ResponseEntity.ok(response);
    }
}
