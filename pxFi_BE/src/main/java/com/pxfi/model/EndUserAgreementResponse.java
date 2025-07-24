package com.pxfi.model;

import java.util.List;

public class EndUserAgreementResponse {
    private String id;
    private String created;
    private int max_historical_days;
    private int access_valid_for_days;
    private List<String> access_scope;
    private String accepted;
    private String institution_id;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }

    public int getMax_historical_days() { return max_historical_days; }
    public void setMax_historical_days(int max_historical_days) { this.max_historical_days = max_historical_days; }

    public int getAccess_valid_for_days() { return access_valid_for_days; }
    public void setAccess_valid_for_days(int access_valid_for_days) { this.access_valid_for_days = access_valid_for_days; }

    public List<String> getAccess_scope() { return access_scope; }
    public void setAccess_scope(List<String> access_scope) { this.access_scope = access_scope; }

    public String getAccepted() { return accepted; }
    public void setAccepted(String accepted) { this.accepted = accepted; }

    public String getInstitution_id() { return institution_id; }
    public void setInstitution_id(String institution_id) { this.institution_id = institution_id; }
}
