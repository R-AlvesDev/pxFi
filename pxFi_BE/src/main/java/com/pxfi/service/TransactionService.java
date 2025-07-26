package com.pxfi.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pxfi.model.Category;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.TransactionRepository;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public void saveTransactions(String accountId, TransactionsResponse transactionsResponse) {
        if (transactionsResponse.getTransactions() == null) return;

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
            String uniqueKey = generateUniqueKeyForTransaction(incomingTx);

            if (processedInThisBatch.contains(uniqueKey)) {
                continue; // Duplicate within this batch
            }

            if (incomingTx.getTransactionId() != null) {
                // If the incoming transaction has an ID
                if (transactionRepository.existsByTransactionIdAndAccountId(incomingTx.getTransactionId(), accountId)) {
                    continue; // Already exists by ID, do nothing.
                }

                // Check if an ID-less version exists
                Optional<Transaction> potentialDuplicate = transactionRepository.findPotentialDuplicate(
                        accountId,
                        incomingTx.getBookingDate(),
                        incomingTx.getTransactionAmount().getAmount(),
                        incomingTx.getRemittanceInformationUnstructured()
                );

                if (potentialDuplicate.isPresent()) {
                    // It's an update! Enrich the existing record with the new ID.
                    Transaction existingTx = potentialDuplicate.get();
                    existingTx.setTransactionId(incomingTx.getTransactionId());
                    transactionsToUpdate.add(existingTx);
                } else {
                    // It's a genuinely new transaction
                    transactionsToInsert.add(incomingTx);
                }
            } else {
                // If the incoming transaction has NO ID, check for duplicates the old way
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

    // Helper method to create a consistent unique identifier for a transaction
    private String generateUniqueKeyForTransaction(Transaction tx) {
        if (tx.getTransactionId() != null) {
            return tx.getTransactionId();
        }
        // Fallback for transactions without a dedicated ID
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
}