package com.pxfi.controller;

import com.pxfi.model.DashboardSummaryResponse;
import com.pxfi.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary/{accountId}")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @PathVariable String accountId) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(accountId);
        return ResponseEntity.ok(summary);
    }
}
