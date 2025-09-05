package com.pxfi.security.auth;

public class AuthenticationResponse {
    private String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    // Getter and Setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}