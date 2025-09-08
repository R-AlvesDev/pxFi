package com.pxfi.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId; // Import ObjectId
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
            return new ArrayList<>(); 
        }
        return ruleRepository.findByUserId(currentUser.getId());
    }

    public CategorizationRule createRule(CategorizationRule rule) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Cannot create rule without a logged in user.");
        }
        // Convert the String ID to an ObjectId before saving
        rule.setUserId(new ObjectId(currentUser.getId()));
        return ruleRepository.save(rule);
    }

    public void deleteRule(String ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    public long applyAllRules() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        List<Transaction> allTransactions = transactionRepository.findAllByUserId(currentUser.getId());
        List<CategorizationRule> allRules = ruleRepository.findByUserId(currentUser.getId());
        List<Transaction> transactionsToUpdate = new ArrayList<>();

        for (Transaction tx : allTransactions) {
            boolean wasModified = ruleEngineService.applyRules(tx, allRules);
            if (wasModified) {
                transactionsToUpdate.add(tx);
            }
        }

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