package com.btxtech.server.frontend;

import com.btxtech.shared.dto.FrontendLoginState;

/**
 * Created by Beat
 * on 03.02.2018.
 */
public class InternalLoginState {
    private FrontendLoginState frontendLoginState;
    private String loginCookieValue;

    public FrontendLoginState getFrontendLoginState() {
        return frontendLoginState;
    }

    public InternalLoginState setFrontendLoginState(FrontendLoginState frontendLoginState) {
        this.frontendLoginState = frontendLoginState;
        return this;
    }

    public String getLoginCookieValue() {
        return loginCookieValue;
    }

    public InternalLoginState setLoginCookieValue(String loginCookieValue) {
        this.loginCookieValue = loginCookieValue;
        return this;
    }
}
