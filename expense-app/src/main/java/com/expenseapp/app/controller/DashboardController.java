package com.expenseapp.app.controller;

import com.expenseapp.app.dto.dashboard.response.DashboardResponse;
import com.expenseapp.app.dto.response.ApiResponse;
import com.expenseapp.app.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardSummary(){
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }
}
