package com.pxfi.service;

import com.pxfi.model.CategorySpending;
import com.pxfi.model.DashboardSummaryResponse;
import com.pxfi.model.StatisticsResponse;
import com.pxfi.model.Transaction;
import com.pxfi.model.User;
import com.pxfi.repository.TransactionRepository;
import com.pxfi.security.SecurityConfiguration;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final StatisticsService statisticsService;

    public DashboardService(TransactionRepository transactionRepository, StatisticsService statisticsService) {
        this.transactionRepository = transactionRepository;
        this.statisticsService = statisticsService;
    }

    public DashboardSummaryResponse getDashboardSummary(String accountId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        String userId = currentUser.getId();

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        // Use the existing statistics service to get monthly totals
        StatisticsResponse monthlyStats = statisticsService.getMonthlyStatistics(accountId, currentYear, currentMonth);

        // Get top 5 spending categories for the current month
        List<CategorySpending> topSpendingCategories = monthlyStats.expensesByCategory().stream()
                .sorted(Comparator.comparing(CategorySpending::total).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Get the 5 most recent transactions for this specific account, sorted by date
        List<Transaction> recentTransactions = transactionRepository
                .findByAccountIdAndUserIdOrderByBookingDateDesc(accountId, userId)
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