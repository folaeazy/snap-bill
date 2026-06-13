package com.expenseapp.app.controller;

import com.domain.entities.User;
import com.expenseapp.app.dto.report.request.ReportQueryRequest;
import com.expenseapp.app.dto.report.response.ReportOverviewResponse;
import com.expenseapp.app.dto.response.AppResponse;
import com.expenseapp.app.interfaces.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = " Report", description = "Report APIs")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUser currentUser;


    @GetMapping("/overview")
    ResponseEntity<AppResponse<ReportOverviewResponse>> getReportOverview(@Valid @ModelAttribute ReportQueryRequest request) {

        User user = currentUser.getCurrentUser();
        ReportOverviewResponse data = reportService.getOverview(user, request);
        var response = AppResponse.<ReportOverviewResponse>builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .data(data)
                .message("report overview fetched")
                .build();

        return ResponseEntity.ok(response);
    }
}
