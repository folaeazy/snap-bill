package com.expenseapp.app.exceptions;

import com.expenseapp.app.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDenyHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(accessDeniedException.getMessage())
                .build();
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
