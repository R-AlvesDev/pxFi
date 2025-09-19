package com.pxfi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoCardlessTokenResponse {

    @JsonProperty("access")
    private String accessToken;

    @JsonProperty("access_expires")
    private long accessExpiresIn;

    @JsonProperty("refresh")
    private String refreshToken;

    @JsonProperty("refresh_expires")
    private long refreshExpiresIn;

    // Constructors
    public GoCardlessTokenResponse() {}

    public GoCardlessTokenResponse(
            String accessToken, long accessExpiresIn, String refreshToken, long refreshExpiresIn) {
        this.accessToken = accessToken;
        this.accessExpiresIn = accessExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getAccessExpiresIn() {
        return accessExpiresIn;
    }

    public void setAccessExpiresIn(long accessExpiresIn) {
        this.accessExpiresIn = accessExpiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }
}
