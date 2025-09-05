package com.pxfi.model;

public class TestRuleRequest {
    private CategorizationRule rule;
    private String accountId;

    // Getters and Setters
    public CategorizationRule getRule() {
        return rule;
    }
    public void setRule(CategorizationRule rule) {
        this.rule = rule;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}