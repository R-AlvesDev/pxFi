package com.pxfi.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal currentMonthIncome;
    private BigDecimal currentMonthExpenses;
    private BigDecimal netBalance;
    private List<CategorySpending> topSpendingCategories;
    private List<Transaction> recentTransactions;

    // Getters and Setters
    public BigDecimal getCurrentMonthIncome() {
        return currentMonthIncome;
    }
    public void setCurrentMonthIncome(BigDecimal currentMonthIncome) {
        this.currentMonthIncome = currentMonthIncome;
    }
    public BigDecimal getCurrentMonthExpenses() {
        return currentMonthExpenses;
    }
    public void setCurrentMonthExpenses(BigDecimal currentMonthExpenses) {
        this.currentMonthExpenses = currentMonthExpenses;
    }
    public BigDecimal getNetBalance() {
        return netBalance;
    }
    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
    public List<CategorySpending> getTopSpendingCategories() {
        return topSpendingCategories;
    }
    public void setTopSpendingCategories(List<CategorySpending> topSpendingCategories) {
        this.topSpendingCategories = topSpendingCategories;
    }
    public List<Transaction> getRecentTransactions() {
        return recentTransactions;
    }
    public void setRecentTransactions(List<Transaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}