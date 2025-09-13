package com.pxfi.service;

import com.pxfi.crypto.EncryptionService;
import com.pxfi.model.Category;
import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.model.User;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.TransactionRepository;
import com.pxfi.security.SecurityConfiguration;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    final TransactionRepository transactionRepository;
    private final CategorizationRuleRepository ruleRepository;
    private final RuleEngineService ruleEngineService;
    private final CategoryRepository categoryRepository;
    private final EncryptionService encryptionService;

    public TransactionService(
        TransactionRepository transactionRepository,
        CategorizationRuleRepository ruleRepository,
        @Lazy RuleEngineService ruleEngineService,
        CategoryRepository categoryRepository,
        EncryptionService encryptionService
    ) {
        this.transactionRepository = transactionRepository;
        this.ruleRepository = ruleRepository;
        this.ruleEngineService = ruleEngineService;
        this.categoryRepository = categoryRepository;
        this.encryptionService = encryptionService;
    }

    public List<Transaction> getTransactionsByAccountId(String accountId, String startDate, String endDate) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) return List.of();
        ObjectId userId = currentUser.getId();

        List<Transaction> transactions;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            transactions = transactionRepository.findByAccountIdAndUserIdAndBookingDateBetweenOrderByBookingDateDesc(accountId, userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByAccountIdAndUserIdOrderByBookingDateDesc(accountId, userId);
        }

        Map<String, Category> categoryMap = categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        return transactions.stream()
            .map(this::decryptTransaction)
            .map(tx -> enrichTransactionWithCategoryNames(tx, categoryMap))
            .collect(Collectors.toList());
    }

    public void saveTransactions(String accountId, TransactionsResponse transactionsResponse) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) throw new IllegalStateException("User not authenticated.");
        ObjectId userId = currentUser.getId();
        if (transactionsResponse.getTransactions() == null || transactionsResponse.getTransactions().getBooked() == null) return;
        
        List<CategorizationRule> allRules = ruleRepository.findByUserId(userId).stream()
            .map(rule -> {
                rule.setValueToMatch(encryptionService.decrypt(rule.getValueToMatch()));
                return rule;
            })
            .collect(Collectors.toList());

        List<Transaction> allUserTransactions = this.getAllTransactions();
        Set<String> existingInternalIds = allUserTransactions.stream()
            .map(Transaction::getInternalTransactionId)
            .collect(Collectors.toSet());

        List<Transaction> transactionsToInsert = transactionsResponse.getTransactions().getBooked().stream()
            .filter(incoming -> incoming.getInternalTransactionId() != null && !incoming.getInternalTransactionId().isEmpty())
            .filter(incoming -> !existingInternalIds.contains(incoming.getInternalTransactionId()))
            .collect(Collectors.toList());

        if (!transactionsToInsert.isEmpty()) {
            Map<String, Category> categoryMap = categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

            transactionsToInsert.forEach(tx -> {
                tx.setAccountId(accountId);
                tx.setUserId(userId);
                ruleEngineService.applyRules(tx, allRules);
                enrichTransactionWithCategoryNames(tx, categoryMap);
                encryptTransaction(tx);
            });
            transactionRepository.saveAll(transactionsToInsert);
        }
    }

    public Transaction updateTransactionCategory(String transactionId, String categoryId, String subCategoryId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) throw new IllegalStateException("User not authenticated.");
        
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or permission denied."));

        // Update the category IDs on the raw transaction object
        transaction.setCategoryId(categoryId);
        transaction.setSubCategoryId(subCategoryId);
        
        // Save the updated transaction back to the database
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // **THE FIX IS HERE**: Decrypt and enrich the saved transaction BEFORE returning it to the frontend.
        Transaction decryptedAndEnrichedTx = decryptTransaction(savedTransaction);
        Map<String, Category> categoryMap = categoryRepository.findByUserId(currentUser.getId()).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        return enrichTransactionWithCategoryNames(decryptedAndEnrichedTx, categoryMap);
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
        if (currentUser == null) throw new IllegalStateException("User not authenticated.");
        ObjectId userId = currentUser.getId();

        Transaction expenseTx = transactionRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense transaction not found."));
        Transaction incomeTx = transactionRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Income transaction not found."));

        expenseTx.setLinkedTransactionId(incomeTx.getId());
        incomeTx.setLinkedTransactionId(expenseTx.getId());
        
        transactionRepository.saveAll(List.of(expenseTx, incomeTx));
    }

    public List<Transaction> getAllTransactions() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) return List.of();
        
        return transactionRepository.findAllByUserId(currentUser.getId()).stream()
            .map(this::decryptTransaction)
            .collect(Collectors.toList());
    }

    public List<Transaction> categorizeSimilarTransactions(String remittanceInfo, String categoryId, String subCategoryId) {

        // This method needs to be made fully user-aware if you continue to use it,

        // but for now, this resolves the compilation errors in other services.

        // It should fetch transactions for the current user only.

        return new ArrayList<>();

    }

    public void saveAllTransactions(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }
    
    private Transaction enrichTransactionWithCategoryNames(Transaction tx, Map<String, Category> categoryMap) {
        if (tx.getCategoryId() != null) {
            Category mainCat = categoryMap.get(tx.getCategoryId());
            if (mainCat != null) tx.setCategoryName(mainCat.getName());
        }
        if (tx.getSubCategoryId() != null) {
            Category subCat = categoryMap.get(tx.getSubCategoryId());
            if (subCat != null) tx.setSubCategoryName(subCat.getName());
        } else {
            tx.setSubCategoryName(null);
        }
        return tx;
    }

    public Transaction encryptTransaction(Transaction tx) {
        if (tx == null) return null;
        tx.setTransactionId(encryptionService.encrypt(tx.getTransactionId()));
        tx.setRemittanceInformationUnstructured(encryptionService.encrypt(tx.getRemittanceInformationUnstructured()));
        if (tx.getTransactionAmount() != null) {
            tx.getTransactionAmount().setAmount(encryptionService.encrypt(tx.getTransactionAmount().getAmount()));
        }
        return tx;
    }

    public Transaction decryptTransaction(Transaction tx) {
        if (tx == null) return null;
        tx.setTransactionId(encryptionService.decrypt(tx.getTransactionId()));
        tx.setRemittanceInformationUnstructured(encryptionService.decrypt(tx.getRemittanceInformationUnstructured()));
        if (tx.getTransactionAmount() != null) {
            tx.getTransactionAmount().setAmount(encryptionService.decrypt(tx.getTransactionAmount().getAmount()));
        }
        return tx;
    }
}

