package com.pxfi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pxfi.model.StatisticsResponse;
import com.pxfi.service.StatisticsService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<StatisticsResponse> getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        StatisticsResponse stats = statisticsService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(stats);
    }
}