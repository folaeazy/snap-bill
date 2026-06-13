package com.expenseapp.app.controller;

import com.domain.entities.User;
import com.expenseapp.app.dto.dashboard.response.DashboardResponse;
import com.expenseapp.app.dto.response.AppResponse;
import com.expenseapp.app.interfaces.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@Tag(name = " Dashboard", description = "Dashboard APIs")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    @Operation(
            summary = "Get dashboard summary",
            description = "Returns user dashboard including summary, recent expenses, and insights"
    )
    @ApiResponses({
            @ApiResponse (responseCode = "200", description = "Successful response"),
    })
    @GetMapping
    public ResponseEntity<AppResponse<DashboardResponse>> getDashboardSummary(){
        User user = currentUser.getCurrentUser();
        DashboardResponse data = dashboardService.getDashboardSummary(user);
        var response = AppResponse.<DashboardResponse>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
