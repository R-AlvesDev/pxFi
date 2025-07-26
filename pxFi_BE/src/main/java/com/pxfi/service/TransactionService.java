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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        if (transactionsResponse.getTransactions() == null) return;

        // Fetch all categorization rules once to use for all transactions in this batch
        List<CategorizationRule> allRules = ruleRepository.findAll();

        List<Transaction> transactionsToInsert = new ArrayList<>();
        List<Transaction> transactionsToUpdate = new ArrayList<>();
        Set<String> processedInThisBatch = new HashSet<>();

        List<Transaction> combinedTransactions = new ArrayList<>();
        if (transactionsResponse.getTransactions().getBooked() != null) {
            combinedTransactions.addAll(transactionsResponse.getTransactions().getBooked());
        }
        if (transactionsResponse.getTransactions().getPending() != null) {
            combinedTransactions.addAll(transactionsResponse.getTransactions().getPending());
        }

        for (Transaction incomingTx : combinedTransactions) {
            incomingTx.setAccountId(accountId);

            // --- APPLY RULES ENGINE ---
            ruleEngineService.applyRules(incomingTx, allRules);
            if (incomingTx.getCategoryId() != null) {
                // If a rule applied a category, fetch and set the category names
                categoryRepository.findById(incomingTx.getCategoryId()).ifPresent(cat -> incomingTx.setCategoryName(cat.getName()));
                if (incomingTx.getSubCategoryId() != null) {
                    categoryRepository.findById(incomingTx.getSubCategoryId()).ifPresent(subCat -> incomingTx.setSubCategoryName(subCat.getName()));
                }
            }

            String uniqueKey = generateUniqueKeyForTransaction(incomingTx);

            if (processedInThisBatch.contains(uniqueKey)) {
                continue; // Duplicate within this batch
            }

            if (incomingTx.getTransactionId() != null) {
                // If the incoming transaction has an ID, check for existence
                if (transactionRepository.existsByTransactionIdAndAccountId(incomingTx.getTransactionId(), accountId)) {
                    continue; // Already exists by ID, do nothing.
                }

                // Check if an ID-less version exists that we can update
                Optional<Transaction> potentialDuplicate = transactionRepository.findPotentialDuplicate(
                        accountId,
                        incomingTx.getBookingDate(),
                        incomingTx.getTransactionAmount().getAmount(),
                        incomingTx.getRemittanceInformationUnstructured()
                );

                if (potentialDuplicate.isPresent()) {
                    // It's an update. Enrich the existing record.
                    Transaction existingTx = potentialDuplicate.get();
                    existingTx.setTransactionId(incomingTx.getTransactionId());
                    // Also apply category if a rule matched
                    existingTx.setCategoryId(incomingTx.getCategoryId());
                    existingTx.setCategoryName(incomingTx.getCategoryName());
                    existingTx.setSubCategoryId(incomingTx.getSubCategoryId());
                    existingTx.setSubCategoryName(incomingTx.getSubCategoryName());
                    transactionsToUpdate.add(existingTx);
                } else {
                    // It's a genuinely new transaction
                    transactionsToInsert.add(incomingTx);
                }
            } else {
                // If the incoming transaction has NO ID, check for duplicates based on content
                if (transactionRepository.existsDuplicate(
                        accountId,
                        incomingTx.getBookingDate(),
                        incomingTx.getTransactionAmount().getAmount(),
                        incomingTx.getRemittanceInformationUnstructured())) {
                    continue; // Duplicate already in DB
                }
                transactionsToInsert.add(incomingTx);
            }

            processedInThisBatch.add(uniqueKey);
        }

        if (!transactionsToInsert.isEmpty()) {
            transactionRepository.saveAll(transactionsToInsert);
        }
        if (!transactionsToUpdate.isEmpty()) {
            transactionRepository.saveAll(transactionsToUpdate);
        }
    }

    private String generateUniqueKeyForTransaction(Transaction tx) {
        if (tx.getTransactionId() != null) {
            return tx.getTransactionId();
        }
        return String.join("|",
                tx.getBookingDate(),
                tx.getTransactionAmount() != null ? tx.getTransactionAmount().getAmount() : "0.00",
                tx.getRemittanceInformationUnstructured() != null ? tx.getRemittanceInformationUnstructured() : "");
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
}