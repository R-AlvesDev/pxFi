package com.pxfi.model;

import java.math.BigDecimal;
import java.util.List;

public record YearlyStatisticsResponse(
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    BigDecimal averageMonthlyIncome,
    BigDecimal averageMonthlyExpenses,
    List<MonthlyBreakdown> monthlyBreakdowns
) {
}