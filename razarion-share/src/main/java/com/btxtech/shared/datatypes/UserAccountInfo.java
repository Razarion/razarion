package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 07.02.2018.
 */
public class UserAccountInfo {
    private String email;
    private boolean rememberMe;

    public String getEmail() {
        return email;
    }

    public UserAccountInfo setEmail(String email) {
        this.email = email;
        return this;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public UserAccountInfo setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
        return this;
    }
}
