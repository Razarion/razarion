package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 29.01.2018.
 */
public class FrontendLoginState {
    private boolean loggedIn;
    private String language;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public FrontendLoginState setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public FrontendLoginState setLanguage(String language) {
        this.language = language;
        return this;
    }
}
