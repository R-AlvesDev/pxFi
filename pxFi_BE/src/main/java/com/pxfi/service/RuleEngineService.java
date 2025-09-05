package com.pxfi.service;

import com.pxfi.model.CategorizationRule;
// Use the correct import syntax for inner enums
import com.pxfi.model.CategorizationRule.RuleField;
import com.pxfi.model.CategorizationRule.RuleOperator;
import com.pxfi.model.Transaction;
import com.pxfi.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleEngineService {

    private final TransactionRepository transactionRepository;

    public RuleEngineService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public boolean applyRules(Transaction transaction, List<CategorizationRule> rules) {
        for (CategorizationRule rule : rules) {
            if (matches(transaction, rule)) {
                transaction.setCategoryId(rule.getCategoryId());
                transaction.setSubCategoryId(rule.getSubCategoryId());
                return true; 
            }
        }
        return false; 
    }

    private boolean matches(Transaction tx, CategorizationRule rule) {
        String valueToMatch = rule.getValueToMatch().toLowerCase();
        String targetValue = getTargetValue(tx, rule.getFieldToMatch());

        if (targetValue == null) {
            return false;
        }

        switch (rule.getOperator()) {
            case CONTAINS:
                return targetValue.toLowerCase().contains(valueToMatch);
            case EQUALS:
                return targetValue.toLowerCase().equals(valueToMatch);
            case STARTS_WITH:
                return targetValue.toLowerCase().startsWith(valueToMatch);
            case ENDS_WITH:
                return targetValue.toLowerCase().endsWith(valueToMatch);
            case AMOUNT_EQUALS:
                return new BigDecimal(targetValue).compareTo(new BigDecimal(valueToMatch)) == 0;
            case AMOUNT_GREATER_THAN:
                return new BigDecimal(targetValue).compareTo(new BigDecimal(valueToMatch)) > 0;
            case AMOUNT_LESS_THAN:
                return new BigDecimal(targetValue).compareTo(new BigDecimal(valueToMatch)) < 0;
            default:
                return false;
        }
    }

    private String getTargetValue(Transaction tx, RuleField field) {
        switch (field) {
            case REMITTANCE_INFO:
                return tx.getRemittanceInformationUnstructured();
            case AMOUNT:
                return tx.getTransactionAmount().getAmount();
            default:
                return null;
        }
    }

    public List<Transaction> testRule(CategorizationRule rule, String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Transaction> allTransactions = transactionRepository.findByAccountIdOrderByBookingDateDesc(accountId);
        
        return allTransactions.stream()
            .filter(tx -> matches(tx, rule))
            .collect(Collectors.toList());
    }
}