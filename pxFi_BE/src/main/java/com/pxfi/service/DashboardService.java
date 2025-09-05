package com.pxfi.service;

import com.pxfi.model.Category;
import com.pxfi.model.CategorySpending;
import com.pxfi.model.DashboardSummaryResponse;
import com.pxfi.model.Transaction;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticsService statisticsService;

    public DashboardService(TransactionRepository transactionRepository, CategoryRepository categoryRepository, StatisticsService statisticsService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.statisticsService = statisticsService;
    }

    public DashboardSummaryResponse getDashboardSummary(String accountId) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        // Use the existing statistics service to get monthly totals
        var monthlyStats = statisticsService.getMonthlyStatistics(currentYear, currentMonth);

        // Get top 5 spending categories for the current month
        List<CategorySpending> topSpendingCategories = monthlyStats.expensesByCategory().stream()
                .sorted(Comparator.comparing(CategorySpending::total).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Get the 5 most recent transactions
        List<Transaction> recentTransactions = transactionRepository
                .findByAccountIdOrderByBookingDateDesc(accountId).stream()
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