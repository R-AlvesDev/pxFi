package com.pxfi.service;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.model.User;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.TransactionRepository;
import com.pxfi.security.SecurityConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategorizationRuleRepository ruleRepository;
    private final RuleEngineService ruleEngineService;
    private final CategoryRepository categoryRepository;

    public TransactionService(
        TransactionRepository transactionRepository,
        CategorizationRuleRepository ruleRepository,
        RuleEngineService ruleEngineService,
        CategoryRepository categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.ruleRepository = ruleRepository;
        this.ruleEngineService = ruleEngineService;
        this.categoryRepository = categoryRepository;
    }

    public void saveTransactions(String accountId, TransactionsResponse transactionsResponse) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        String userId = currentUser.getId();

        if (transactionsResponse.getTransactions() == null || transactionsResponse.getTransactions().getBooked() == null) {
            return;
        }
        
        List<CategorizationRule> allRules = ruleRepository.findByUserId(userId);

        List<Transaction> incomingTxs = transactionsResponse.getTransactions().getBooked().stream()
            .filter(t -> t.getInternalTransactionId() != null && !t.getInternalTransactionId().isEmpty())
            .collect(Collectors.toList());

        if (incomingTxs.isEmpty()) return;

        Set<String> existingInternalIds = transactionRepository.findAllByUserId(userId).stream()
            .map(Transaction::getInternalTransactionId)
            .collect(Collectors.toSet());

        List<Transaction> transactionsToInsert = incomingTxs.stream()
            .filter(incoming -> !existingInternalIds.contains(incoming.getInternalTransactionId()))
            .collect(Collectors.toList());

        if (!transactionsToInsert.isEmpty()) {
            transactionsToInsert.forEach(tx -> {
                tx.setAccountId(accountId);
                tx.setUserId(userId); // Set the owner
                ruleEngineService.applyRules(tx, allRules);
                if (tx.getCategoryId() != null) {
                    categoryRepository.findById(tx.getCategoryId()).ifPresent(cat -> tx.setCategoryName(cat.getName()));
                    if (tx.getSubCategoryId() != null) {
                        categoryRepository.findById(tx.getSubCategoryId()).ifPresent(subCat -> tx.setSubCategoryName(subCat.getName()));
                    }
                }
            });
            transactionRepository.saveAll(transactionsToInsert);
        }
    }

    public List<Transaction> getTransactionsByAccountId(String accountId, String startDate, String endDate) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        String userId = currentUser.getId();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            return transactionRepository.findByAccountIdAndUserIdAndBookingDateBetweenOrderByBookingDateDesc(accountId, userId, startDate, endDate);
        }
        return transactionRepository.findByAccountIdAndUserIdOrderByBookingDateDesc(accountId, userId);
    }

    public Transaction updateTransactionCategory(String transactionId, String categoryId, String subCategoryId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or permission denied."));

        transaction.setCategoryId(categoryId);
        transaction.setSubCategoryId(subCategoryId);
        
        categoryRepository.findById(categoryId).ifPresent(cat -> transaction.setCategoryName(cat.getName()));
        if (subCategoryId != null) {
            categoryRepository.findById(subCategoryId).ifPresent(subCat -> transaction.setSubCategoryName(subCat.getName()));
        } else {
            transaction.setSubCategoryName(null);
        }

        return transactionRepository.save(transaction);
    }
    
    public Optional<Transaction> toggleTransactionIgnoreStatus(String transactionId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) return Optional.empty();

        return transactionRepository.findByIdAndUserId(transactionId, currentUser.getId()).map(transaction -> {
            transaction.setIgnored(!transaction.isIgnored());
            return transactionRepository.save(transaction);
        });
    }

    @Transactional
    public void linkTransactions(String expenseId, String incomeId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        String userId = currentUser.getId();

        Transaction expenseTx = transactionRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense transaction not found."));
        Transaction incomeTx = transactionRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Income transaction not found."));

        expenseTx.setLinkedTransactionId(incomeTx.getId());
        incomeTx.setLinkedTransactionId(expenseTx.getId());
        
        transactionRepository.saveAll(List.of(expenseTx, incomeTx));
    }

    public List<Transaction> categorizeSimilarTransactions(String remittanceInfo, String categoryId, String subCategoryId) {
        // This method needs to be made fully user-aware if you continue to use it,
        // but for now, this resolves the compilation errors in other services.
        // It should fetch transactions for the current user only.
        return new ArrayList<>();
    }

    public List<Transaction> getAllTransactions() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        return transactionRepository.findAllByUserId(currentUser.getId());
    }
}