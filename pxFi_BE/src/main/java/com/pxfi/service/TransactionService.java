package com.pxfi.service;

import com.pxfi.model.Category;
import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategorizationRuleRepository ruleRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    public void saveTransactions(String accountId, TransactionsResponse transactionsResponse) {
        if (transactionsResponse.getTransactions() == null || transactionsResponse.getTransactions().getBooked() == null) {
            return;
        }

        List<CategorizationRule> allRules = ruleRepository.findAll();

        List<Transaction> incomingTxs = transactionsResponse.getTransactions().getBooked().stream()
            .filter(t -> t.getInternalTransactionId() != null && !t.getInternalTransactionId().isEmpty())
            .collect(Collectors.toList());

        if (incomingTxs.isEmpty()) {
            return;
        }

        Set<String> existingInternalIds = transactionRepository.findAll().stream()
            .filter(t -> t.getInternalTransactionId() != null)
            .map(Transaction::getInternalTransactionId)
            .collect(Collectors.toSet());

        List<Transaction> transactionsToInsert = incomingTxs.stream()
            .filter(incoming -> !existingInternalIds.contains(incoming.getInternalTransactionId()))
            .collect(Collectors.toList());

        if (!transactionsToInsert.isEmpty()) {
            transactionsToInsert.forEach(tx -> {
                tx.setAccountId(accountId);
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

    public List<Transaction> getTransactionsByAccountId(String accountId) {
        return transactionRepository.findByAccountIdOrderByBookingDateDesc(accountId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction updateTransactionCategory(String transactionId, String categoryId, String subCategoryId) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
        if (optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();

            Optional<Category> mainCategory = categoryRepository.findById(categoryId);
            Optional<Category> subCategory = subCategoryId != null ? categoryRepository.findById(subCategoryId) : Optional.empty();

            if (mainCategory.isPresent()) {
                transaction.setCategoryId(mainCategory.get().getId());
                transaction.setCategoryName(mainCategory.get().getName());

                if (subCategory.isPresent() && subCategory.get().getParentId().equals(mainCategory.get().getId())) {
                    transaction.setSubCategoryId(subCategory.get().getId());
                    transaction.setSubCategoryName(subCategory.get().getName());
                } else {
                    transaction.setSubCategoryId(null);
                    transaction.setSubCategoryName(null);
                }
                return transactionRepository.save(transaction);
            }
        }
        return null;
    }
    
    public List<Transaction> categorizeSimilarTransactions(String remittanceInfo, String categoryId, String subCategoryId) {
        Category mainCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid main category ID"));
        
        Category subCategory = null;
        if (subCategoryId != null) {
            subCategory = categoryRepository.findById(subCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid sub category ID"));
            if (!subCategory.getParentId().equals(mainCategory.getId())) {
                throw new IllegalArgumentException("Subcategory does not belong to the main category.");
            }
        }

        List<Transaction> transactionsToUpdate;
        if (subCategoryId != null) {
            transactionsToUpdate = transactionRepository
                .findByRemittanceInformationUnstructuredAndCategoryIdAndSubCategoryIdIsNull(remittanceInfo, categoryId);
        } else {
            transactionsToUpdate = transactionRepository
                .findByRemittanceInformationUnstructuredAndCategoryIdIsNull(remittanceInfo);
        }

        for (Transaction tx : transactionsToUpdate) {
            tx.setCategoryId(mainCategory.getId());
            tx.setCategoryName(mainCategory.getName());
            if (subCategory != null) {
                tx.setSubCategoryId(subCategory.getId());
                tx.setSubCategoryName(subCategory.getName());
            }
        }

        if (!transactionsToUpdate.isEmpty()) {
            transactionRepository.saveAll(transactionsToUpdate);
        }
        
        return transactionsToUpdate;
    }

    public Optional<Transaction> toggleTransactionIgnoreStatus(String transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> {
                    transaction.setIgnored(!transaction.isIgnored());
                    return transactionRepository.save(transaction);
                });
    }

    @Transactional // Ensures both saves happen together or not at all
    public void linkTransactions(String expenseId, String incomeId) {
        // Find both transactions from the database
        Transaction expenseTx = transactionRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense transaction not found"));
        Transaction incomeTx = transactionRepository.findById(incomeId)
                .orElseThrow(() -> new IllegalArgumentException("Income transaction not found"));

        // Link them to each other
        expenseTx.setLinkedTransactionId(incomeTx.getId());
        incomeTx.setLinkedTransactionId(expenseTx.getId());

        // Save both updated transactions back to the database
        transactionRepository.saveAll(List.of(expenseTx, incomeTx));
    }
}