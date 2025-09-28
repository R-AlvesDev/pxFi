package com.pxfi.service;

import com.pxfi.crypto.EncryptionService;
import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Category;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.model.User;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.TransactionRepository;
import com.pxfi.security.SecurityConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategorizationRuleRepository ruleRepository;
    private final RuleEngineService ruleEngineService;
    private final CategoryRepository categoryRepository;
    private final EncryptionService encryptionService;

    public TransactionService(
            TransactionRepository transactionRepository,
            CategorizationRuleRepository ruleRepository,
            @Lazy RuleEngineService ruleEngineService,
            CategoryRepository categoryRepository,
            EncryptionService encryptionService) {
        this.transactionRepository = transactionRepository;
        this.ruleRepository = ruleRepository;
        this.ruleEngineService = ruleEngineService;
        this.categoryRepository = categoryRepository;
        this.encryptionService = encryptionService;
    }

    public List<Transaction> getTransactionsByAccountId(
            String accountId, String startDate, String endDate) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        ObjectId userId = currentUser.getId();

        List<Transaction> transactions;
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            transactions = transactionRepository
                    .findByAccountIdAndUserIdAndBookingDateBetweenOrderByBookingDateDesc(
                            accountId, userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByAccountIdAndUserIdOrderByBookingDateDesc(
                    accountId, userId);
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
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        ObjectId userId = currentUser.getId();
        if (transactionsResponse.getTransactions() == null
                || transactionsResponse.getTransactions().getBooked() == null) {
            return;
        }
        List<CategorizationRule> allRules = ruleRepository.findByUserId(userId).stream()
                .map(
                        rule -> {
                            rule.setValueToMatch(
                                    encryptionService.decrypt(rule.getValueToMatch()));
                            return rule;
                        })
                .collect(Collectors.toList());

        List<Transaction> allUserTransactions = this.getAllTransactions();
        Set<String> existingInternalIds = allUserTransactions.stream()
                .map(Transaction::getInternalTransactionId)
                .collect(Collectors.toSet());

        List<Transaction> transactionsToInsert = transactionsResponse.getTransactions().getBooked().stream()
                .filter(
                        incoming -> incoming.getInternalTransactionId() != null
                                && !incoming.getInternalTransactionId().isEmpty())
                .filter(
                        incoming -> !existingInternalIds.contains(
                                incoming.getInternalTransactionId()))
                .collect(Collectors.toList());

        if (!transactionsToInsert.isEmpty()) {
            Map<String, Category> categoryMap = categoryRepository.findByUserId(userId).stream()
                    .collect(Collectors.toMap(Category::getId, Function.identity()));

            transactionsToInsert.forEach(
                    tx -> {
                        tx.setAccountId(accountId);
                        tx.setUserId(userId);
                        ruleEngineService.applyRules(tx, allRules);
                        enrichTransactionWithCategoryNames(tx, categoryMap);
                        encryptTransaction(tx);
                    });
            transactionRepository.saveAll(transactionsToInsert);
        }
    }

    public Transaction updateTransactionCategory(
            String transactionId, String categoryId, String subCategoryId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        Transaction transaction = transactionRepository
                .findByIdAndUserId(transactionId, currentUser.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Transaction not found or permission denied."));

        transaction.setCategoryId(categoryId);
        transaction.setSubCategoryId(subCategoryId);

        Transaction savedTransaction = transactionRepository.save(transaction);

        Transaction decryptedAndEnrichedTx = decryptTransaction(savedTransaction);
        Map<String, Category> categoryMap = categoryRepository.findByUserId(currentUser.getId()).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        return enrichTransactionWithCategoryNames(decryptedAndEnrichedTx, categoryMap);
    }

    public Optional<Transaction> toggleTransactionIgnoreStatus(String transactionId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return Optional.empty();
        }
        return transactionRepository
                .findByIdAndUserId(transactionId, currentUser.getId())
                .map(
                        transaction -> {
                            transaction.setIgnored(!transaction.isIgnored());
                            Transaction savedTransaction = transactionRepository.save(transaction);

                            Transaction decryptedTx = decryptTransaction(savedTransaction);
                            Map<String, Category> categoryMap = categoryRepository.findByUserId(currentUser.getId())
                                    .stream()
                                    .collect(
                                            Collectors.toMap(
                                                    Category::getId, Function.identity()));
                            return enrichTransactionWithCategoryNames(decryptedTx, categoryMap);
                        });
    }

    @Transactional
    public void linkTransactions(String expenseId, String incomeId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated.");
        }
        ObjectId userId = currentUser.getId();

        Transaction expenseTx = transactionRepository
                .findByIdAndUserId(expenseId, userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Expense transaction not found."));
        Transaction incomeTx = transactionRepository
                .findByIdAndUserId(incomeId, userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Income transaction not found."));

        expenseTx.setLinkedTransactionId(incomeTx.getId());
        incomeTx.setLinkedTransactionId(expenseTx.getId());

        transactionRepository.saveAll(List.of(expenseTx, incomeTx));
    }

    public List<Transaction> categorizeSimilarTransactions(
            String remittanceInfo,
            String categoryId,
            String subCategoryId,
            boolean isAddingSubcategory) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        List<Transaction> allUserTransactions = this.getAllTransactions();

        String normalizedRemittanceInfo = remittanceInfo.trim().replaceAll("\\s+", " ");

        List<Transaction> transactionsToUpdate = allUserTransactions.stream()
                .filter(
                        tx -> {
                            String txRemittance = tx.getRemittanceInformationUnstructured();
                            if (txRemittance == null) {
                                return false;
                            }
                            String normalizedTxRemittance = txRemittance.trim().replaceAll("\\s+", " ");
                            return normalizedRemittanceInfo.equals(normalizedTxRemittance);
                        })
                .filter(
                        tx -> {
                            if (isAddingSubcategory) {
                                return categoryId.equals(tx.getCategoryId())
                                        && tx.getSubCategoryId() == null;
                            } else {
                                return tx.getCategoryId() == null;
                            }
                        })
                .collect(Collectors.toList());

        if (!transactionsToUpdate.isEmpty()) {
            Map<String, Category> categoryMap = categoryRepository.findByUserId(currentUser.getId()).stream()
                    .collect(Collectors.toMap(Category::getId, Function.identity()));

            transactionsToUpdate.forEach(
                    tx -> {
                        tx.setCategoryId(categoryId);
                        tx.setSubCategoryId(subCategoryId);
                        enrichTransactionWithCategoryNames(tx, categoryMap);
                        encryptTransaction(tx);
                    });
            transactionRepository.saveAll(transactionsToUpdate);
        }

        return transactionsToUpdate.stream()
                .map(this::decryptTransaction)
                .collect(Collectors.toList());
    }

    public List<Transaction> getAllTransactions() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }
        ObjectId userId = currentUser.getId();

        return transactionRepository.findAllByUserId(userId).stream()
                .map(this::decryptTransaction)
                .collect(Collectors.toList());
    }

    public void saveAllTransactions(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    private Transaction enrichTransactionWithCategoryNames(
            Transaction tx, Map<String, Category> categoryMap) {
        if (tx.getCategoryId() != null) {
            Category mainCat = categoryMap.get(tx.getCategoryId());
            if (mainCat != null) {
                tx.setCategoryName(mainCat.getName());
            }
        }
        if (tx.getSubCategoryId() != null) {
            Category subCat = categoryMap.get(tx.getSubCategoryId());
            if (subCat != null) {
                tx.setSubCategoryName(subCat.getName());
            }
        } else {
            tx.setSubCategoryName(null);
        }
        return tx;
    }

    public Transaction encryptTransaction(Transaction tx) {
        if (tx == null) {
            return null;
        }
        if (tx.getTransactionAmount() != null) {
            tx.getTransactionAmount()
                    .setAmount(encryptionService.encrypt(tx.getTransactionAmount().getAmount()));
        }
        tx.setRemittanceInformationUnstructured(
                encryptionService.encrypt(tx.getRemittanceInformationUnstructured()));
        tx.setTransactionId(encryptionService.encrypt(tx.getTransactionId()));
        return tx;
    }

    public Transaction decryptTransaction(Transaction tx) {
        if (tx == null) {
            return null;
        }
        if (tx.getTransactionAmount() != null) {
            tx.getTransactionAmount()
                    .setAmount(encryptionService.decrypt(tx.getTransactionAmount().getAmount()));
        }
        tx.setRemittanceInformationUnstructured(
                encryptionService.decrypt(tx.getRemittanceInformationUnstructured()));
        tx.setTransactionId(encryptionService.decrypt(tx.getTransactionId()));
        return tx;
    }
}
