package com.pxfi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Transaction;
import com.pxfi.repository.CategorizationRuleRepository;
import com.pxfi.repository.TransactionRepository;

@Service
public class CategorizationRuleService {

    private final CategorizationRuleRepository ruleRepository;
    private final TransactionRepository transactionRepository;
    private final RuleEngineService ruleEngineService;

    public CategorizationRuleService(CategorizationRuleRepository ruleRepository, TransactionRepository transactionRepository, RuleEngineService ruleEngineService) {
        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
        this.ruleEngineService = ruleEngineService;
    }

    public List<CategorizationRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public CategorizationRule createRule(CategorizationRule rule) {
        return ruleRepository.save(rule);
    }

    public long applyAllRules() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<CategorizationRule> allRules = ruleRepository.findAll();

        List<Transaction> updatedTransactions = allTransactions.stream()
                .filter(tx -> {
                    // Keep track of category before applying rules
                    String originalCategoryId = tx.getCategoryId();
                    ruleEngineService.applyRules(tx, allRules);
                    // Return true only if the category has changed
                    return tx.getCategoryId() != null && !tx.getCategoryId().equals(originalCategoryId);
                })
                .collect(Collectors.toList());

        if (!updatedTransactions.isEmpty()) {
            transactionRepository.saveAll(updatedTransactions);
        }

        return updatedTransactions.size();
    }

    public void deleteRule(String ruleId) {
        ruleRepository.deleteById(ruleId);
    }
}