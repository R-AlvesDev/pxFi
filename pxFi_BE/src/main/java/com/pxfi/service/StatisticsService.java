package com.pxfi.service;

import com.pxfi.model.*;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.security.SecurityConfiguration;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;

    public StatisticsService(@Lazy TransactionService transactionService, CategoryRepository categoryRepository) {
        this.transactionService = transactionService;
        this.categoryRepository = categoryRepository;
    }

    public StatisticsResponse getMonthlyStatistics(String accountId, int year, int month) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        ObjectId userId = currentUser.getId();

        String startDate = LocalDate.of(year, month, 1).toString();
        String endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).toString();

        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId, startDate, endDate);
        Map<String, Category> categoryMap = categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        Set<String> processedIds = new HashSet<>();
        Map<String, BigDecimal> expensesByCategoryMap = new HashMap<>();

        for (Transaction tx : transactions) {
            if (tx.getLinkedTransactionId() != null && !processedIds.contains(tx.getId())) {
                Transaction linkedTx = transactions.stream()
                    .filter(t -> t.getId().equals(tx.getLinkedTransactionId()))
                    .findFirst().orElse(null);
                
                if (linkedTx != null) {
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
                    totalExpenses = totalExpenses.add(amount.abs());
                    String categoryName = tx.getCategoryName() != null ? tx.getCategoryName() : "Uncategorized";
                    expensesByCategoryMap.merge(categoryName, amount.abs(), BigDecimal::add);
                }
            }
        }
        
        List<CategorySpending> expensesByCategory = expensesByCategoryMap.entrySet().stream()
            .map(entry -> new CategorySpending(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(CategorySpending::total).reversed())
            .collect(Collectors.toList());

        return new StatisticsResponse(userId, totalIncome, totalExpenses, expensesByCategory);
    }
    
    public YearlyStatisticsResponse getYearlyStatistics(String accountId, int year) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        ObjectId userId = currentUser.getId();

        String startDate = LocalDate.of(year, 1, 1).toString();
        String endDate = LocalDate.of(year, 12, 31).toString();
        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId, startDate, endDate);
        
        Map<String, Category> categoryMap = categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, cat -> cat));

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

