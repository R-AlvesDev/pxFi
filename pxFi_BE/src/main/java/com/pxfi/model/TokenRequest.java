package com.pxfi.model;

public class TokenRequest {
    private String secret_id;
    private String secret_key;

    public TokenRequest() {}

    public TokenRequest(String secret_id, String secret_key) {
        this.secret_id = secret_id;
        this.secret_key = secret_key;
    }

    public String getSecret_id() {
        return secret_id;
    }

    public void setSecret_id(String secret_id) {
        this.secret_id = secret_id;
    }

    public String getSecret_key() {
        return secret_key;
    }

    public void setSecret_key(String secret_key) {
        this.secret_key = secret_key;
    }
}
