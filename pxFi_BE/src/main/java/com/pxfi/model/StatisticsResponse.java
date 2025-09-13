package com.pxfi.model;

import java.math.BigDecimal;
import java.util.List;

import org.bson.types.ObjectId;

public record StatisticsResponse(
    ObjectId userId,
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    List<CategorySpending> expensesByCategory
) {
}