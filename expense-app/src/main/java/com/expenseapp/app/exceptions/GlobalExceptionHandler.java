package com.expenseapp.app.exceptions;


import com.expenseapp.app.dto.response.ApiResponse;
import com.domain.exceptions.DomainValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainValidationException ex, WebRequest req) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(req.getDescription(false))
                .build();
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);


    }

    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .success(false)
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .path(request.getDescription(false))
                .build();

        // Log the real exception (don't expose stack trace to client)
        ex.printStackTrace(); // or use proper logger

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
