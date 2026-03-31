package com.expenseapp.app.service;

import com.expenseapp.app.dto.report.request.ReportQueryRequest;
import com.expenseapp.app.dto.report.response.ReportOverviewResponse;
import com.expenseapp.app.interfaces.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
public class ReportServiceImpl implements ReportService {
    /**
     * @param request
     * @return
     */
    @Override
    public ReportOverviewResponse getOverview(ReportQueryRequest request) {
        return null;
    }
}
