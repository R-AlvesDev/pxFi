package com.pxfi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.TestRuleRequest;
import com.pxfi.model.TestRuleResponse;
import com.pxfi.model.Transaction;
import com.pxfi.model.User;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.CategoryRepository;
import com.pxfi.repository.TransactionRepository;
import com.pxfi.security.SecurityConfiguration;

@Service
public class CategorizationRuleService {

    private final CategorizationRuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;
    private final RuleEngineService ruleEngineService;
    private final CategoryRepository categoryRepository;

    public CategorizationRuleService(
        CategorizationRuleRepository ruleRepository,
        TransactionRepository transactionRepository,
        RuleEngineService ruleEngineService,
        CategoryRepository categoryRepository
    ) {
        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
        this.ruleEngineService = ruleEngineService;
        this.categoryRepository = categoryRepository;
    }

    public List<CategorizationRule> getAllRules() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            // Or throw an exception, returning empty list is safer for read operations
            return new ArrayList<>(); 
        }
        return ruleRepository.findByUserId(currentUser.getId());
    }

    public CategorizationRule createRule(CategorizationRule rule) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Cannot create rule without a logged in user.");
        }
        rule.setUserId(currentUser.getId()); // Set the owner of the new rule
        return ruleRepository.save(rule);
    }

    public void deleteRule(String ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    public long applyAllRules() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<CategorizationRule> allRules = ruleRepository.findAll();
        List<Transaction> transactionsToUpdate = new ArrayList<>();

        System.out.println("--- Applying " + allRules.size() + " rules to " + allTransactions.size() + " transactions. ---");

        for (Transaction tx : allTransactions) {
            // The engine will now return true if it modifies the transaction
            boolean wasModified = ruleEngineService.applyRules(tx, allRules);

            if (wasModified) {
                // If changed, add it to our list to be saved.
                transactionsToUpdate.add(tx);
            }
        }

        // Now, update the category names for ONLY the transactions that changed.
        transactionsToUpdate.forEach(tx -> {
            categoryRepository.findById(tx.getCategoryId()).ifPresent(cat -> tx.setCategoryName(cat.getName()));
            if (tx.getSubCategoryId() != null) {
                categoryRepository.findById(tx.getSubCategoryId()).ifPresent(subCat -> tx.setSubCategoryName(subCat.getName()));
            } else {
                tx.setSubCategoryName(null);
            }
        });

        if (!transactionsToUpdate.isEmpty()) {
            transactionRepository.saveAll(transactionsToUpdate);
        }

        System.out.println("--- Finished applying rules. " + transactionsToUpdate.size() + " transactions were updated. ---");
        return transactionsToUpdate.size();
    }

    public TestRuleResponse testRule(TestRuleRequest request) {
        if (request.getRule() == null || request.getAccountId() == null) {
            throw new IllegalArgumentException("Rule and Account ID must be provided for testing.");
        }
        
        List<Transaction> matchedTransactions = ruleEngineService.testRule(
            request.getRule(),
            request.getAccountId()
        );
        
        return new TestRuleResponse(matchedTransactions);
    }
}