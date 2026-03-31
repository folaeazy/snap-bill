package com.expenseapp.app.interfaces;

import com.expenseapp.app.dto.report.request.ReportQueryRequest;
import com.expenseapp.app.dto.report.response.ReportOverviewResponse;

public interface ReportService {
    ReportOverviewResponse getOverview(ReportQueryRequest request);
}
