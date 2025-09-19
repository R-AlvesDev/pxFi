package com.pxfi.model;

import java.math.BigDecimal;

public record MonthlyBreakdown(int month, BigDecimal income, BigDecimal expenses) {}
