package com.pxfi.model;

import java.util.List;

public class EndUserAgreementResponse {
    private String id;
    private String created;
    private int maxHistoricalDays;
    private int accessValidForDays;
    private List<String> accessScope;
    private String accepted;
    private String institutionId;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getMaxHistoricalDays() {
        return maxHistoricalDays;
    }

    public void setMaxHistoricalDays(int maxHistoricalDays) {
        this.maxHistoricalDays = maxHistoricalDays;
    }

    public int getAccessValidForDays() {
        return accessValidForDays;
    }

    public void setAccessValidForDays(int accessValidForDays) {
        this.accessValidForDays = accessValidForDays;
    }

    public List<String> getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(List<String> accessScope) {
        this.accessScope = accessScope;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }
}
