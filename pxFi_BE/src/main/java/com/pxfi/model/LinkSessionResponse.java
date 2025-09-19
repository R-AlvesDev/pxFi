package com.pxfi.model;

public class LinkSessionResponse {
    private String redirectUrl;
    private String accessToken;

    public LinkSessionResponse() {}

    public LinkSessionResponse(String redirectUrl, String accessToken) {
        this.redirectUrl = redirectUrl;
        this.accessToken = accessToken;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
