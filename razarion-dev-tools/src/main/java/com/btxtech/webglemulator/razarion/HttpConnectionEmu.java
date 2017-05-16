package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.dto.ColdGameUiControlConfig;

import javax.ws.rs.core.NewCookie;

/**
 * Created by Beat
 * 21.04.2017.
 */
public class HttpConnectionEmu {
    public static final String SESSION_KEY = "JSESSIONID";
    private static HttpConnectionEmu INSTANCE;
    private ColdGameUiControlConfig coldGameUiControlConfig;
    private NewCookie sessionCookie;

    public HttpConnectionEmu() {
        INSTANCE = this;
    }

    public ColdGameUiControlConfig getColdGameUiControlConfig() {
        return coldGameUiControlConfig;
    }

    public void setColdGameUiControlConfig(ColdGameUiControlConfig coldGameUiControlConfig) {
        this.coldGameUiControlConfig = coldGameUiControlConfig;
    }

    public void setSessionCookie(NewCookie sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public NewCookie getSessionCookie() {
        return sessionCookie;
    }

    public static HttpConnectionEmu getInstance() {
        return INSTANCE;
    }
}
