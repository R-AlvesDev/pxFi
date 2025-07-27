package com.pxfi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pxfi.model.CategorySpending;
import com.pxfi.model.StatisticsResponse;
import com.pxfi.model.Transaction;
import com.pxfi.repository.TransactionRepository;

@Service
public class StatisticsService {

    private final TransactionRepository transactionRepository;

    public StatisticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public StatisticsResponse getMonthlyStatistics(int year, int month) {
        // Define the date range for the query
        String startDate = LocalDate.of(year, month, 1).toString();
        String endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).toString();

        // Fetch all transactions for the given month
        List<Transaction> transactions = transactionRepository.findByBookingDateBetween(startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        // Calculate total income and expenses by iterating through the transactions
        for (Transaction tx : transactions) {
            BigDecimal amount = new BigDecimal(tx.getTransactionAmount().getAmount());
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(amount);
            } else {
                totalExpenses = totalExpenses.add(amount.abs());
            }
        }

        // Use the aggregation query to get spending grouped by category
        List<CategorySpending> expensesByCategory = transactionRepository.findSpendingByCategory(startDate, endDate);

        return new StatisticsResponse(totalIncome, totalExpenses, expensesByCategory);
    }
}