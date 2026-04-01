package com.expenseapp.app.service;

import com.expenseapp.app.dto.dashboard.response.DashboardResponse;
import com.expenseapp.app.interfaces.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
    /**
     * @return
     */
    @Override
    public DashboardResponse getDashboardSummary() {
        return null;
    }
}
