package com.pxfi.controller;

import com.pxfi.model.StatisticsResponse;
import com.pxfi.model.YearlyStatisticsResponse;
import com.pxfi.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly/{accountId}")
    public StatisticsResponse getMonthlyStatistics(
            @PathVariable String accountId, @RequestParam int year, @RequestParam int month) {
        return statisticsService.getMonthlyStatistics(accountId, year, month);
    }

    @GetMapping("/yearly/{accountId}")
    public YearlyStatisticsResponse getYearlyStatistics(
            @PathVariable String accountId, @RequestParam int year) {
        return statisticsService.getYearlyStatistics(accountId, year);
    }
}
