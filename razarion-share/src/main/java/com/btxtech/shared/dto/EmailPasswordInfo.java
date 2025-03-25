package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 06.02.2018.
 */
public class EmailPasswordInfo {
    private String email;
    private String password;
    private boolean rememberMe;

    public String getEmail() {
        return email;
    }

    public EmailPasswordInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public EmailPasswordInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public EmailPasswordInfo setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
        return this;
    }
}
