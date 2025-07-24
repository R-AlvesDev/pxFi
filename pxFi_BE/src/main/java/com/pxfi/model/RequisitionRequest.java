package com.pxfi.model;

public class RequisitionRequest {
    private String redirect;
    private String institution_id;
    private String agreement;
    private String reference;

    public RequisitionRequest() {}

    public RequisitionRequest(String redirect, String institution_id, String agreement, String reference) {
        this.redirect = redirect;
        this.institution_id = institution_id;
        this.agreement = agreement;
        this.reference = reference;
    }

    // Getters and Setters
    public String getRedirect() { return redirect; }
    public void setRedirect(String redirect) { this.redirect = redirect; }

    public String getInstitution_id() { return institution_id; }
    public void setInstitution_id(String institution_id) { this.institution_id = institution_id; }

    public String getAgreement() { return agreement; }
    public void setAgreement(String agreement) { this.agreement = agreement; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
