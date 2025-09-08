package com.pxfi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private String userId;
    private String gocardlessAccountId; // The ID from the GoCardless API
    private String accountName; // User-defined alias
    private String institutionId; // The ID of the bank (e.g., "REVOLUT_REVOGB21")
    private String iban;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getGocardlessAccountId() { return gocardlessAccountId; }
    public void setGocardlessAccountId(String gocardlessAccountId) { this.gocardlessAccountId = gocardlessAccountId; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getInstitutionId() { return institutionId; }
    public void setInstitutionId(String institutionId) { this.institutionId = institutionId; }
    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }
}