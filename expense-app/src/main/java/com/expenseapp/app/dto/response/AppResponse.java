package com.expenseapp.app.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(hidden = true)
public class AppResponse<T> {

    private int statusCode;

    private String message;

    private boolean success;

    private T data;

    private String path;

    private Instant timestamp = Instant.now();
}
