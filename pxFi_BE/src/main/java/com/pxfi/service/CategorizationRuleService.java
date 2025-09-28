package com.pxfi.service;

import com.pxfi.crypto.EncryptionService;
import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Category;
import com.pxfi.model.TestRuleRequest;
import com.pxfi.model.TestRuleResponse;
import com.pxfi.model.Transaction;
import com.pxfi.model.User;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.security.SecurityConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bson.types.ObjectId; // Import ObjectId if it's not already there
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CategorizationRuleService {

    private final CategorizationRuleRepository ruleRepository;
    private final TransactionService transactionService;
    private final RuleEngineService ruleEngineService;
    private final CategoryRepository categoryRepository;
    private final EncryptionService encryptionService;

    public CategorizationRuleService(
            CategorizationRuleRepository ruleRepository,
            @Lazy TransactionService transactionService,
            @Lazy RuleEngineService ruleEngineService,
            CategoryRepository categoryRepository,
            EncryptionService encryptionService) {
        this.ruleRepository = ruleRepository;
        this.transactionService = transactionService;
        this.ruleEngineService = ruleEngineService;
        this.categoryRepository = categoryRepository;
        this.encryptionService = encryptionService;
    }

    public List<CategorizationRule> getAllRules() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return ruleRepository.findByUserId(currentUser.getId()).stream()
                .map(this::decryptRule)
                .collect(Collectors.toList());
    }

    public CategorizationRule createRule(CategorizationRule rule) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Cannot create rule without a logged in user.");
        }
        if (rule.getFieldToMatch() == null || rule.getOperator() == null) {
            throw new IllegalArgumentException("Rule must have a field and operator.");
        }
        rule.setUserId(currentUser.getId());
        return ruleRepository.save(encryptRule(rule));
    }

    public void deleteRule(String ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    public long applyAllRules() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return 0;
        }

        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<CategorizationRule> allRules = this.getAllRules();

        List<Transaction> transactionsToUpdate = new ArrayList<>();

        for (Transaction tx : allTransactions) {
            boolean wasModified = ruleEngineService.applyRules(tx, allRules);
            if (wasModified) {
                transactionsToUpdate.add(tx);
            }
        }

        if (!transactionsToUpdate.isEmpty()) {
            Map<String, Category> categoryMap = categoryRepository.findByUserId(currentUser.getId()).stream()
                    .collect(Collectors.toMap(Category::getId, Function.identity()));

            transactionsToUpdate.forEach(
                    tx -> {
                        if (tx.getCategoryId() != null) {
                            Category mainCat = categoryMap.get(tx.getCategoryId().toString());
                            if (mainCat != null) {
                                tx.setCategoryName(mainCat.getName());
                            }
                        }
                        if (tx.getSubCategoryId() != null) {
                            Category subCat = categoryMap.get(tx.getSubCategoryId().toString());
                            if (subCat != null) {
                                tx.setSubCategoryName(subCat.getName());
                            }
                        } else {
                            tx.setSubCategoryName(null);
                        }
                        transactionService.encryptTransaction(tx);
                    });

            transactionService.saveAllTransactions(transactionsToUpdate);
        }

        return transactionsToUpdate.size();
    }

    public TestRuleResponse testRule(TestRuleRequest request) {
        if (request.getRule() == null || request.getAccountId() == null) {
            throw new IllegalArgumentException("Rule and Account ID must be provided for testing.");
        }

        List<Transaction> matchedTransactions = ruleEngineService.testRule(request.getRule(), request.getAccountId());

        return new TestRuleResponse(matchedTransactions);
    }

    // --- Helper Methods for Encryption/Decryption ---

    public CategorizationRule encryptRule(CategorizationRule rule) {
        if (rule == null) {
            return null;
        }
        rule.setValueToMatch(encryptionService.encrypt(rule.getValueToMatch()));
        return rule;
    }

    public CategorizationRule decryptRule(CategorizationRule rule) {
        if (rule == null) {
            return null;
        }
        rule.setValueToMatch(encryptionService.decrypt(rule.getValueToMatch()));
        return rule;
    }
}