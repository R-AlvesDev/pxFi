package com.pxfi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EndUserAgreementResponse {
    private String id;
    private String created;

    @JsonProperty("max_historical_days")
    private int maxHistoricalDays;

    @JsonProperty("access_valid_for_days")
    private int accessValidForDays;

    @JsonProperty("access_scope")
    private List<String> accessScope;

    private String accepted;

    @JsonProperty("institution_id")
    private String institutionId;

    // Getters and setters remain the same
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