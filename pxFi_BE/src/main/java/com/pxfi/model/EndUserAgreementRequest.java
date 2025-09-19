package com.pxfi.model;

import java.util.List;

public class EndUserAgreementRequest {
    private String institutionId;
    private String maxHistoricalDays;
    private String accessValidForDays;
    private List<String> accessScope;

    public EndUserAgreementRequest() {}

    public EndUserAgreementRequest(
            String institutionId,
            String maxHistoricalDays,
            String accessValidForDays,
            List<String> accessScope) {
        this.institutionId = institutionId;
        this.maxHistoricalDays = maxHistoricalDays;
        this.accessValidForDays = accessValidForDays;
        this.accessScope = accessScope;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getMaxHistoricalDays() {
        return maxHistoricalDays;
    }

    public void setMaxHistoricalDays(String maxHistoricalDays) {
        this.maxHistoricalDays = maxHistoricalDays;
    }

    public String getAccessValidForDays() {
        return accessValidForDays;
    }

    public void setAccessValidForDays(String accessValidForDays) {
        this.accessValidForDays = accessValidForDays;
    }

    public List<String> getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(List<String> accessScope) {
        this.accessScope = accessScope;
    }
}
