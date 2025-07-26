package com.pxfi.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Transaction;

@Service
public class RuleEngineService {

    public void applyRules(Transaction transaction, List<CategorizationRule> rules) {

        for (CategorizationRule rule : rules) {
            boolean matches = false;
            switch (rule.getFieldToMatch()) {
                case REMITTANCE_INFO:
                    matches = checkRemittanceInfo(transaction, rule);
                    break;
                case AMOUNT:
                    matches = checkAmount(transaction, rule);
                    break;
            }

            if (matches) {
                System.out.println("MATCH FOUND: Rule " + rule.getId() + " matched transaction with remittance: '" + transaction.getRemittanceInformationUnstructured() + "'");
                transaction.setCategoryId(rule.getCategoryId());
                transaction.setSubCategoryId(rule.getSubCategoryId());
                // Stop after the first rule matches to avoid conflicting rules.
                return;
            }
        }
    }

    private boolean checkRemittanceInfo(Transaction transaction, CategorizationRule rule) {
        String info = transaction.getRemittanceInformationUnstructured();
        String value = rule.getValueToMatch();

        // More robust check for null or empty strings
        if (info == null || info.trim().isEmpty() || value == null || value.trim().isEmpty()) {
            return false;
        }

        String trimmedInfo = info.trim();
        String trimmedValue = value.trim();

        switch (rule.getOperator()) {
            case CONTAINS:
                return trimmedInfo.toLowerCase().contains(trimmedValue.toLowerCase());
            case EQUALS:
                return trimmedInfo.equalsIgnoreCase(trimmedValue);
            case STARTS_WITH:
                return trimmedInfo.toLowerCase().startsWith(trimmedValue.toLowerCase());
            case ENDS_WITH:
                return trimmedInfo.toLowerCase().endsWith(trimmedValue.toLowerCase());
            default:
                return false;
        }
    }

    private boolean checkAmount(Transaction transaction, CategorizationRule rule) {
        if (transaction.getTransactionAmount() == null || transaction.getTransactionAmount().getAmount() == null) {
            return false;
        }
        try {
            BigDecimal transactionAmount = new BigDecimal(transaction.getTransactionAmount().getAmount());
            BigDecimal ruleAmount = new BigDecimal(rule.getValueToMatch());

            switch (rule.getOperator()) {
                case GREATER_THAN:
                    return transactionAmount.compareTo(ruleAmount) > 0;
                case LESS_THAN:
                    return transactionAmount.compareTo(ruleAmount) < 0;
                case EQUALS:
                    return transactionAmount.compareTo(ruleAmount) == 0;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            // If rule value is not a number, it can't match amount rules
            return false;
        }
    }
}