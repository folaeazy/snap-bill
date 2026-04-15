package com.expenseapp.app.controller;

import com.domain.entities.User;
import com.expenseapp.app.dto.dashboard.response.DashboardResponse;
import com.expenseapp.app.dto.response.ApiResponse;
import com.expenseapp.app.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUser currentUser;

    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardSummary(){
        User user = currentUser.getCurrentUser();
        DashboardResponse data = dashboardService.getDashboardSummary(user);
        var response = ApiResponse.<DashboardResponse>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}
