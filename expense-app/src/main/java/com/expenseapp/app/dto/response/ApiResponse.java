package com.expenseapp.app.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int statusCode;

    private String message;

    private boolean success;

    private T data;

    private String path;

    private Instant timestamp = Instant.now();
}
