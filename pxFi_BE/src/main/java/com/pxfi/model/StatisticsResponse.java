package com.pxfi.model;

import java.math.BigDecimal;
import java.util.List;

public record StatisticsResponse(
    String userId,
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    List<CategorySpending> expensesByCategory
) {
}