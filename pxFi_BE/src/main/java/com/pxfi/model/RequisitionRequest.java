package com.pxfi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequisitionRequest {
    private String redirect;

    @JsonProperty("institution_id") 
    private String institutionId;

    private String agreement;
    private String reference;

    public RequisitionRequest() {}

    public RequisitionRequest(
            String redirect, String institutionId, String agreement, String reference) {
        this.redirect = redirect;
        this.institutionId = institutionId;
        this.agreement = agreement;
        this.reference = reference;
    }

    // Getters and Setters remain the same
    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}