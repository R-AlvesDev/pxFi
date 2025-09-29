package com.pxfi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EndUserAgreementRequest {
    @JsonProperty("institution_id")
    private String institutionId;

    @JsonProperty("max_historical_days")
    private int maxHistoricalDays;

    @JsonProperty("access_valid_for_days")
    private int accessValidForDays;

    @JsonProperty("access_scope")
    private List<String> accessScope;

    public EndUserAgreementRequest() {}

    public EndUserAgreementRequest(
            String institutionId,
            int maxHistoricalDays,
            int accessValidForDays,
            List<String> accessScope) {
        this.institutionId = institutionId;
        this.maxHistoricalDays = maxHistoricalDays;
        this.accessValidForDays = accessValidForDays;
        this.accessScope = accessScope;
    }

    // Getters and setters remain the same
    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
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
}