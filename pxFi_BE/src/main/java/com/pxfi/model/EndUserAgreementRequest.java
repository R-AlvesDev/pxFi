package com.pxfi.model;

import java.util.List;

public class EndUserAgreementRequest {
    private String institution_id;
    private String max_historical_days;
    private String access_valid_for_days;
    private List<String> access_scope;

    public EndUserAgreementRequest() {}

    public EndUserAgreementRequest(String institution_id, String max_historical_days, String access_valid_for_days, List<String> access_scope) {
        this.institution_id = institution_id;
        this.max_historical_days = max_historical_days;
        this.access_valid_for_days = access_valid_for_days;
        this.access_scope = access_scope;
    }

    public String getInstitution_id() {
        return institution_id;
    }

    public void setInstitution_id(String institution_id) {
        this.institution_id = institution_id;
    }

    public String getMax_historical_days() {
        return max_historical_days;
    }

    public void setMax_historical_days(String max_historical_days) {
        this.max_historical_days = max_historical_days;
    }

    public String getAccess_valid_for_days() {
        return access_valid_for_days;
    }

    public void setAccess_valid_for_days(String access_valid_for_days) {
        this.access_valid_for_days = access_valid_for_days;
    }

    public List<String> getAccess_scope() {
        return access_scope;
    }

    public void setAccess_scope(List<String> access_scope) {
        this.access_scope = access_scope;
    }
}
