package com.pxfi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pxfi.model.Category;
import com.pxfi.model.CategorySpending;
import com.pxfi.model.MonthlyBreakdown;
import com.pxfi.model.StatisticsResponse;
import com.pxfi.model.Transaction;
import com.pxfi.model.YearlyStatisticsResponse;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.TransactionRepository;

@Service
public class StatisticsService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public StatisticsService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public StatisticsResponse getMonthlyStatistics(int year, int month) {
        String startDate = LocalDate.of(year, month, 1).toString();
        String endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).toString();
        List<Transaction> transactions = transactionRepository.findByBookingDateBetween(startDate, endDate);
        Map<String, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        Set<String> processedIds = new HashSet<>();
        Map<String, BigDecimal> spendingPerCategory = new HashMap<>();
    
        // Find the ID of the actual "Uncategorized" category to merge expenses into
        Optional<String> uncategorizedCategoryIdOpt = categoryMap.entrySet().stream()
            .filter(entry -> "Uncategorized".equalsIgnoreCase(entry.getValue().getName()))
            .map(Map.Entry::getKey)
            .findFirst();
    
        for (Transaction tx : transactions) {
            if (tx.getLinkedTransactionId() != null && !processedIds.contains(tx.getId())) {
                Transaction linkedTx = transactions.stream()
                        .filter(t -> t.getId().equals(tx.getLinkedTransactionId()))
                        .findFirst().orElse(null);
                
                if (linkedTx != null) {
                    BigDecimal amount1 = new BigDecimal(tx.getTransactionAmount().getAmount());
                    BigDecimal amount2 = new BigDecimal(linkedTx.getTransactionAmount().getAmount());
                    BigDecimal netExpense = amount1.add(amount2).abs();
                    totalExpenses = totalExpenses.add(netExpense);
    
                    // Use the transaction's category, or fallback to the main "Uncategorized" ID
                    String categoryIdToUse = tx.getCategoryId() != null ? tx.getCategoryId() : uncategorizedCategoryIdOpt.orElse(null);
                    if (categoryIdToUse != null) {
                        spendingPerCategory.merge(categoryIdToUse, netExpense, BigDecimal::add);
                    }
    
                    processedIds.add(tx.getId());
                    processedIds.add(linkedTx.getId());
                }
            }
        }
    
        for (Transaction tx : transactions) {
            if (processedIds.contains(tx.getId()) || tx.isIgnored()) {
                continue;
            }
    
            BigDecimal amount = new BigDecimal(tx.getTransactionAmount().getAmount());
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(amount);
            } else {
                Category categoryToCheck = null;
                if (tx.getSubCategoryId() != null) {
                    categoryToCheck = categoryMap.get(tx.getSubCategoryId());
                } else if (tx.getCategoryId() != null) {
                    categoryToCheck = categoryMap.get(tx.getCategoryId());
                }
    
                if (categoryToCheck == null || !categoryToCheck.isAssetTransfer()) {
                    BigDecimal expenseAmount = amount.abs();
                    totalExpenses = totalExpenses.add(expenseAmount);
                    
                    // If the transaction has a category, use it. Otherwise, use the main "Uncategorized" category ID.
                    String categoryIdToUse = tx.getCategoryId() != null ? tx.getCategoryId() : uncategorizedCategoryIdOpt.orElse(null);
                    if (categoryIdToUse != null) {
                        spendingPerCategory.merge(categoryIdToUse, expenseAmount, BigDecimal::add);
                    }
                }
            }
        }
    
        List<CategorySpending> expensesByCategory = spendingPerCategory.entrySet().stream()
            .map(entry -> {
                String categoryId = entry.getKey();
                Category category = categoryMap.get(categoryId);
                // Ensure the name is always correct, even for the uncategorized group
                String categoryName = (category != null) ? category.getName() : "Uncategorized";
                return new CategorySpending(categoryName, entry.getValue());
            })
            .collect(Collectors.toList());
    
        return new StatisticsResponse(totalIncome, totalExpenses, expensesByCategory);
    }
    

    public YearlyStatisticsResponse getYearlyStatistics(int year) {
        String startDate = LocalDate.of(year, 1, 1).toString();
        String endDate = LocalDate.of(year, 12, 31).toString();
        List<Transaction> transactions = transactionRepository.findByBookingDateBetween(startDate, endDate);
        Map<String, Category> categoryMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        Map<Integer, List<Transaction>> transactionsByMonth = transactions.stream()
                .filter(tx -> !tx.isIgnored())
                .collect(Collectors.groupingBy(tx -> LocalDate.parse(tx.getBookingDate()).getMonthValue()));
        
        List<MonthlyBreakdown> monthlyBreakdowns = new ArrayList<>();
        BigDecimal totalYearlyIncome = BigDecimal.ZERO;
        BigDecimal totalYearlyExpenses = BigDecimal.ZERO;
        int monthsWithIncome = 0;
        int monthsWithExpenses = 0;

        for (int month = 1; month <= 12; month++) {
            List<Transaction> monthlyTransactions = transactionsByMonth.getOrDefault(month, List.of());
            BigDecimal monthlyIncome = BigDecimal.ZERO;
            BigDecimal monthlyExpenses = BigDecimal.ZERO;
            Set<String> processedIds = new HashSet<>();

            for (Transaction tx : monthlyTransactions) {
                if (tx.getLinkedTransactionId() != null && !processedIds.contains(tx.getId())) {
                     Transaction linkedTx = monthlyTransactions.stream()
                        .filter(t -> t.getId().equals(tx.getLinkedTransactionId()))
                        .findFirst().orElse(null);
                    
                    if (linkedTx != null) {
                        BigDecimal netExpense = new BigDecimal(tx.getTransactionAmount().getAmount())
                                .add(new BigDecimal(linkedTx.getTransactionAmount().getAmount())).abs();
                        monthlyExpenses = monthlyExpenses.add(netExpense);
                        processedIds.add(tx.getId());
                        processedIds.add(linkedTx.getId());
                    }
                }
            }
            
            for (Transaction tx : monthlyTransactions) {
                 if (processedIds.contains(tx.getId())) continue;

                BigDecimal amount = new BigDecimal(tx.getTransactionAmount().getAmount());
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    monthlyIncome = monthlyIncome.add(amount);
                } else {
                    Category categoryToCheck = null;
                    if (tx.getSubCategoryId() != null) {
                        categoryToCheck = categoryMap.get(tx.getSubCategoryId());
                    } else if (tx.getCategoryId() != null) {
                        categoryToCheck = categoryMap.get(tx.getCategoryId());
                    }

                    if (categoryToCheck == null || !categoryToCheck.isAssetTransfer()) {
                        monthlyExpenses = monthlyExpenses.add(amount.abs());
                    }
                }
            }
            
            monthlyBreakdowns.add(new MonthlyBreakdown(month, monthlyIncome, monthlyExpenses));
            totalYearlyIncome = totalYearlyIncome.add(monthlyIncome);
            totalYearlyExpenses = totalYearlyExpenses.add(monthlyExpenses);

            if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) monthsWithIncome++;
            if (monthlyExpenses.compareTo(BigDecimal.ZERO) > 0) monthsWithExpenses++;
        }

        BigDecimal avgMonthlyIncome = (monthsWithIncome > 0)
                ? totalYearlyIncome.divide(new BigDecimal(monthsWithIncome), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal avgMonthlyExpenses = (monthsWithExpenses > 0)
                ? totalYearlyExpenses.divide(new BigDecimal(monthsWithExpenses), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new YearlyStatisticsResponse(
            totalYearlyIncome, 
            totalYearlyExpenses, 
            avgMonthlyIncome, 
            avgMonthlyExpenses, 
            monthlyBreakdowns
        );
    }
}