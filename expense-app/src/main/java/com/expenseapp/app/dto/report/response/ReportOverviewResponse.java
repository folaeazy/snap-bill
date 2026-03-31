package com.expenseapp.app.dto.report.response;

import com.expenseapp.app.dto.report.models.CategoryDistribution;
import com.expenseapp.app.dto.report.models.MonthlyTrend;
import com.expenseapp.app.dto.report.models.Summary;
import com.expenseapp.app.dto.report.models.TopMerchant;
import lombok.Builder;

import java.util.List;

@Builder
public record ReportOverviewResponse(
        Summary summary,
        List<MonthlyTrend> monthlyTrends,
        List<CategoryDistribution> categoryDistributions,
        List<TopMerchant> topMerchants
) { }
