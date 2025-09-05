package com.pxfi.model;

import java.util.List;

public class TestRuleResponse {
    private List<Transaction> matchedTransactions;
    private int matchCount;

    public TestRuleResponse(List<Transaction> matchedTransactions) {
        this.matchedTransactions = matchedTransactions;
        this.matchCount = matchedTransactions.size();
    }

    // Getters and Setters
    public List<Transaction> getMatchedTransactions() {
        return matchedTransactions;
    }
    public void setMatchedTransactions(List<Transaction> matchedTransactions) {
        this.matchedTransactions = matchedTransactions;
    }
    public int getMatchCount() {
        return matchCount;
    }
    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }
}