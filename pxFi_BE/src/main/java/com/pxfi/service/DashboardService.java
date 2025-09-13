package com.pxfi.service;

import com.pxfi.model.CategorySpending;
import com.pxfi.model.DashboardSummaryResponse;
import com.pxfi.model.StatisticsResponse;
import com.pxfi.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionService transactionService;
    private final StatisticsService statisticsService;

    public DashboardService(TransactionService transactionService, StatisticsService statisticsService) {
        this.transactionService = transactionService;
        this.statisticsService = statisticsService;
    }

    public DashboardSummaryResponse getDashboardSummary(String accountId) {

        StatisticsResponse monthlyStats = statisticsService.getMonthlyStatistics(accountId, 
            java.time.LocalDate.now().getYear(), 
            java.time.LocalDate.now().getMonthValue());

        List<CategorySpending> topSpendingCategories = monthlyStats.expensesByCategory().stream()
                .sorted(Comparator.comparing(CategorySpending::total).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Transaction> recentTransactions = transactionService
                .getTransactionsByAccountId(accountId, null, null)
                .stream()
                .sorted(Comparator.comparing(Transaction::getBookingDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setCurrentMonthIncome(monthlyStats.totalIncome());
        summary.setCurrentMonthExpenses(monthlyStats.totalExpenses());
        summary.setNetBalance(monthlyStats.totalIncome().subtract(monthlyStats.totalExpenses()));
        summary.setTopSpendingCategories(topSpendingCategories);
        summary.setRecentTransactions(recentTransactions);

        return summary;
    }
}

