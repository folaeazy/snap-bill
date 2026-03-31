package com.expenseapp.app.interfaces;

import com.expenseapp.app.dto.dashboard.response.DashboardResponse;
import com.expenseapp.app.dto.response.ApiResponse;

public interface DashboardService {
    ApiResponse<DashboardResponse> getDashboardSummary();
}
