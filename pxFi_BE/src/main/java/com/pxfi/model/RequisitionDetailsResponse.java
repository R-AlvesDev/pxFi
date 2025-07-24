package com.pxfi.model;

import java.util.List;

public class RequisitionDetailsResponse {
    private String id;
    private String status;
    private String agreements;
    private List<String> accounts;
    private String reference;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAgreements() { return agreements; }
    public void setAgreements(String agreements) { this.agreements = agreements; }

    public List<String> getAccounts() { return accounts; }
    public void setAccounts(List<String> accounts) { this.accounts = accounts; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
