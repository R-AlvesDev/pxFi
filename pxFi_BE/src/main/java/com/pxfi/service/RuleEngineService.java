package com.pxfi.service;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.CategorizationRule.RuleField;
import com.pxfi.model.Transaction;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleEngineService {

    private final TransactionService transactionService;

    // Use @Lazy to prevent potential circular dependency issues during application startup.
    public RuleEngineService(@Lazy TransactionService transactionService) {
        this.transactionService = transactionService;
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
        // Normalize the value from the rule: trim, lowercase, and collapse multiple whitespace
        String valueToMatch = rule.getValueToMatch() == null ? "" : rule.getValueToMatch().trim().replaceAll("\\s+", " ").toLowerCase();
        
        // Normalize the value from the transaction
        String targetValue = getTargetValue(tx, rule.getFieldToMatch());
        targetValue = targetValue == null ? "" : targetValue.trim().replaceAll("\\s+", " ").toLowerCase();

        switch (rule.getOperator()) {
            case CONTAINS:
                return targetValue.contains(valueToMatch);
            case EQUALS:
                return targetValue.equals(valueToMatch);
            case STARTS_WITH:
                return targetValue.startsWith(valueToMatch);
            case ENDS_WITH:
                return targetValue.endsWith(valueToMatch);
            case AMOUNT_EQUALS:
                try {
                    return new BigDecimal(targetValue).compareTo(new BigDecimal(valueToMatch)) == 0;
                } catch (NumberFormatException e) { return false; }
            case AMOUNT_GREATER_THAN:
                 try {
                    return new BigDecimal(targetValue).compareTo(new BigDecimal(valueToMatch)) > 0;
                } catch (NumberFormatException e) { return false; }
            case AMOUNT_LESS_THAN:
                 try {
                    return new BigDecimal(targetValue).compareTo(new BigDecimal(valueToMatch)) < 0;
                } catch (NumberFormatException e) { return false; }
            default:
                return false;
        }
    }

    private String getTargetValue(Transaction tx, RuleField field) {
        if (tx == null) return null;
        switch (field) {
            case REMITTANCE_INFO:
                return tx.getRemittanceInformationUnstructured();
            case AMOUNT:
                return (tx.getTransactionAmount() != null) ? tx.getTransactionAmount().getAmount() : null;
            default:
                return null;
        }
    }

    public List<Transaction> testRule(CategorizationRule rule, String accountId) {
        List<Transaction> allTransactions = transactionService.getTransactionsByAccountId(accountId, null, null);
        
        return allTransactions.stream()
            .filter(tx -> matches(tx, rule))
            .collect(Collectors.toList());
    }
}

