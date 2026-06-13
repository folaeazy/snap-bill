package com.expenseapp.app.dto.dashboard.response;

import com.expenseapp.app.dto.dashboard.models.AiInsight;
import com.expenseapp.app.dto.dashboard.models.RecentExpense;
import com.expenseapp.app.dto.dashboard.models.SavingsGoal;
import com.expenseapp.app.dto.dashboard.models.SummaryCards;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(hidden = true)
public record DashboardResponse(
        String userName,

        SummaryCards summary,

        List<RecentExpense> recentExpenses,

        AiInsight aiInsight,

        SavingsGoal savingsGoal

) { }
