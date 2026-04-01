package com.expenseapp.app.interfaces;

import com.expenseapp.app.dto.dashboard.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardSummary();
}
