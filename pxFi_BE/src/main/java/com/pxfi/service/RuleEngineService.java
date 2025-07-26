package com.pxfi.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.Transaction;

@Service
public class RuleEngineService {

    public boolean applyRules(Transaction transaction, List<CategorizationRule> rules) {
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
                transaction.setCategoryId(rule.getCategoryId());
                transaction.setSubCategoryId(rule.getSubCategoryId());
                return true;
            }
        }
        return false;
    }

    private boolean checkRemittanceInfo(Transaction transaction, CategorizationRule rule) {
        String info = transaction.getRemittanceInformationUnstructured();
        String value = rule.getValueToMatch();
        if (info == null || value == null) return false;

        // --- THE FIX: Normalize whitespace in both strings ---
        String normalizedInfo = info.trim().replaceAll("\\s+", " ");
        String normalizedValue = value.trim().replaceAll("\\s+", " ");
        // --- END FIX ---

        switch (rule.getOperator()) {
            case CONTAINS:
                return normalizedInfo.toLowerCase().contains(normalizedValue.toLowerCase());
            case EQUALS:
                return normalizedInfo.equalsIgnoreCase(normalizedValue);
            case STARTS_WITH:
                return normalizedInfo.toLowerCase().startsWith(normalizedValue.toLowerCase());
            case ENDS_WITH:
                return normalizedInfo.toLowerCase().endsWith(normalizedValue.toLowerCase());
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
            return false;
        }
    }
}