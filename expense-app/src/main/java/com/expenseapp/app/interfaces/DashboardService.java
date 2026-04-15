package com.expenseapp.app.interfaces;

import com.domain.entities.User;
import com.expenseapp.app.dto.dashboard.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardSummary(User user);
}
