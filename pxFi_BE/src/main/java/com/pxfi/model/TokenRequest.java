package com.pxfi.model;

public class TokenRequest {
    private String secretId;
    private String secretKey;

    public TokenRequest() {}

    public TokenRequest(String secretId, String secretKey) {
        this.secretId = secretId;
        this.secretKey = secretKey;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
